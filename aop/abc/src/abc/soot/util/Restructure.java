/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Laurie Hendren
 * Copyright (C) 2004 Ondrej Lhotak
 * Copyright (C) 2004 Jennifer Lhotak
 * Copyright (C) 2004 Sascha Kuzins
 * Copyright (C) 2004 Ganesh Sittampalam
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package abc.soot.util;

import java.util.*;

import polyglot.util.InternalCompilerError;

import soot.*;
import soot.util.*;
import soot.jimple.*;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.toolkits.scalar.*;

import abc.weaving.weaver.*;
import abc.weaving.aspectinfo.MethodCategory;



/** This class contains a variety of helper methods to restructure Soot
 *    method Bodies.
 *
 * @author Laurie Hendren
 * @author Ondrej Lhotak
 * @author Jennifer Lhotak
 * @author Sascha Kuzins
 * @author Ganesh Sittampalam
 */

public class Restructure {

    public static void reset() {
	//returns=new Hashtable();
	//thiscopies=new Hashtable();
	invokeassignstmts=new Hashtable();
    }

   private static void debug(String message)
     { if (abc.main.Debug.v().restructure)
         System.err.println("RST*** " + message);
     }

   /* ------------------- Utility restructurers ----------------------- */

  /** Given a Chain for a body of an <init> method, find the call to
   *  the <init> corresponding to either a this() or super() call.  Check
   *  that there is exactly one such <init>, otherwise throw a 
   *  InternalCompilerError.   Return the <init> Stmt.
   */
  public static InvokeStmt findInitStmt(Chain units)
    { // look for the <init> 
      Iterator it = units.snapshotIterator();
      InvokeStmt initstmt = null;

      // need to track all locals containing refs to "this"
      LinkedList receivercopies = new LinkedList();

      // get the "this", should be first identity statement
      Stmt first = (Stmt) it.next();
      Local thisloc = null;
      if ( (first instanceof IdentityStmt) && 
	   ((IdentityStmt) first).getRightOp() instanceof ThisRef
	 )
        { thisloc = (Local) 
	        ((IdentityStmt) first).getLeftOp();//the local for "this" 
          // add to list of locals containing this
          receivercopies.add(thisloc);
	}
	else
	  throw new InternalCompilerError("Expecting an identity stmt for this");
	
      int countinits = 0;
      debug("--- Starting to look through statement list ..... ");
      while ( it.hasNext() )
        { Stmt u = (Stmt) it.next();
          debug(" ... Looking at stmt " + u);

	  // if we find a stmt lhs = rhs, where rhs is already a copy
	  //     of "this",  add lhs to receivercopies
	  if ((u instanceof AssignStmt) &&
	      receivercopies.contains(((AssignStmt) u).getRightOp()))
	    receivercopies.add(((AssignStmt) u).getLeftOp());

          if ((u instanceof InvokeStmt) && 
	      ((InvokeStmt) u).getInvokeExpr() instanceof SpecialInvokeExpr &&
	      ((InvokeStmt) u).getInvokeExpr().getMethodRef().name()
	      .equals(SootMethod.constructorName) &&
	      receivercopies.contains
	      (((SpecialInvokeExpr) ((InvokeStmt) u).getInvokeExpr()).getBase()))
	    { debug("Found <init> " + u);
	      countinits++;
	      if (countinits == 1) // great, found it
	        initstmt = (InvokeStmt) u;  
	      else
	        throw new InternalCompilerError("Expecting only one <init>");
             }	 
          } // all units
	 
       debug("--- Finished looking through statement list ..... ");
       if (countinits == 0)     
         throw new InternalCompilerError("Could not find a matching <init>");

       return(initstmt);
     }

   /** Given a Chain for the body of a method,  find the first "real"
    *  stmt (i.e. not an identity stmt or the copy of "this" we often make) 
    *  and return a reference to it.
    */
    public static Stmt findFirstRealStmt(SootMethod m,Chain units)
    {
	return findFirstRealStmt(m,units,false);
    }

    public static Stmt findFirstRealStmtOrNop(SootMethod m,Chain units)
    {
	return findFirstRealStmt(m,units,true);
    }

    private static Stmt findFirstRealStmt(SootMethod m,Chain units,boolean allowNops)
     { Iterator it = units.snapshotIterator();
       while ( it.hasNext() ) { 
	     Stmt u = (Stmt) it.next();
	     if(u instanceof IdentityStmt) continue;
	     if(u instanceof NopStmt && !allowNops) continue;
	     // skip over any copy of "this" we made
	     //if(u instanceof AssignStmt)
		 //if(thiscopies.containsKey(m)) 
		   //  if(((AssignStmt) u).getLeftOp()==thiscopies.get(m))
			 //continue;
	     return u;
          }
        throw new InternalCompilerError("Expecting to find a real stmt");
      }
    public static Stmt findFirstRealStmtOrNull(SootMethod m,Chain units)
    { 
    	Iterator it = units.iterator();
      while ( it.hasNext() ) { 
	     Stmt u = (Stmt) it.next();
	     if(u instanceof IdentityStmt) 
	     	continue;
	     return u;
       }
       return null;
     }
   /** update all traps that used to end at oldend to now end at newend
    */
   public static void resetTrapsEnd(Body b, Stmt oldend, Stmt newend)
     { for( Iterator trIt = b.getTraps().iterator(); trIt.hasNext(); ) 
         { final Trap tr = (Trap) trIt.next();
           if( tr.getEndUnit() == oldend ) 
	     tr.setEndUnit(newend);  
	  }
      }

   /** update all traps that used to start at oldstart to now start at newstart
    */
   public static void resetTrapsStart(Body b, Stmt oldstart, Stmt newstart)
     { for( Iterator trIt = b.getTraps().iterator(); trIt.hasNext(); ) 
         { final Trap tr = (Trap) trIt.next();
           if( tr.getBeginUnit() == oldstart ) 
	     tr.setBeginUnit(newstart);  
	  }
      }

   // This caching is not safe because around lifts shadows out of their bodies,
   // in which case the restructuring should be redone for subsequent applications.
  //private static Map/*<Body,Stmt>*/ returns=new Hashtable();

  /** Given a SootMethod, restructure its body so that the body ends
   *  with   L1:nop; return;    or   L1:nop; return(<local>);.
   *  Rewire all other returns in the body to assign to <local> and
   *  goto L1.   Return a reference to the nop at L1.
   */
  public static Stmt restructureReturn(SootMethod method) {
		Body b = method.getActiveBody();
		//if (returns.containsKey(b)) {			
		//	return ((Stmt) returns.get(b));
		//}
		LocalGenerator localgen = new LocalGenerator(b);
		
		Chain units = b.getUnits(); // want a patching chain here, to make sure
		// gotos are rewired to go to the inserted nop
		Stmt last = (Stmt) units.getLast();
		
		Stmt endnop;// = Jimple.v().newNopStmt();
		try { // preserve existing endnop
			endnop=(NopStmt)units.getPredOf(last);
			if (endnop==null)
				throw new RuntimeException();
		} catch(Throwable e) {
			endnop=Jimple.v().newNopStmt();
//			 insert the nop just before the return stmt
			if (last instanceof ReturnStmt || last instanceof ReturnVoidStmt) {
				units.insertBefore(endnop, units.getLast());
			} else {
				units.insertAfter(endnop, units.getLast());
			}
			if (!units.contains(endnop))
				throw new InternalCompilerError("");
		}
		if (!units.contains(endnop))
			throw new InternalCompilerError("");
		
		Local ret = null;
		if (last instanceof ReturnStmt) {
			ReturnStmt lastRet = (ReturnStmt) last;
			Value op = lastRet.getOp();
			if (op instanceof Local) {// return(<local>)
				ret = (Local)op;
				Type returnType = method.getReturnType();
				Local tmp = localgen.generateLocal(returnType);
				// Make sure returned local is used in the shadow once
				// by assigning it to a temporary and back.
				// This makes the around weaver's analyses simpler.			
				units.insertBefore(Jimple.v().newAssignStmt(tmp, op), endnop);
				units.insertBefore(Jimple.v().newAssignStmt(op, tmp), endnop);
			} else if (op instanceof Constant) // return(<constant>)
			{ // change to ret := <constant>; return(ret);
				Type returnType = method.getReturnType();
				ret = localgen.generateLocal(returnType);
				if (!units.contains(endnop))
					throw new InternalCompilerError("");
				units.insertBefore(Jimple.v().newAssignStmt(ret, op), endnop);
				lastRet.setOp(ret);
			} else
				throw new InternalCompilerError(
						"Expecting return of <local> or <constant>");
		} else if (last instanceof ReturnVoidStmt) { // do nothing
		} else {
			Type returnType = method.getReturnType();
			if (returnType instanceof VoidType) {
				units.insertAfter(Jimple.v().newReturnVoidStmt(), endnop);
			} else {
				ret = localgen.generateLocal(returnType);
				units.insertAfter(Jimple.v().newReturnStmt(ret), endnop);
			}
		}

		// now the last stmt should always be return ret; or return;
		if (units.getLast() instanceof ReturnStmt) {
			ReturnStmt lastRet = (ReturnStmt) units.getLast();
			if (lastRet.getOp() != ret)
				throw new InternalCompilerError(
						"Expecting last stmt to Return ret");
		} else if (!(units.getLast() instanceof ReturnVoidStmt)) {
			throw new InternalCompilerError(
					"Last stmt should be ReturnStmt or ReturnVoidStmt");
		}

		
		

		resetTrapsEnd(b, (Stmt) units.getLast(), endnop);

		// Look for returns in the middle of the method
		Iterator it = units.snapshotIterator();
		while (it.hasNext()) {
			Stmt u = (Stmt) it.next();
			if (u == units.getLast())
				continue;
			if (u instanceof ReturnStmt) {
				ReturnStmt ur = (ReturnStmt) u;
				units.insertBefore(Jimple.v().newAssignStmt(ret, ur.getOp()),
						ur);
			}
			if (u instanceof ReturnVoidStmt || u instanceof ReturnStmt) {
				Stmt gotoStmt = Jimple.v().newGotoStmt(endnop);
				units.swapWith(u, gotoStmt);
				for (Iterator trIt = b.getTraps().iterator(); trIt.hasNext();) {
					final Trap tr = (Trap) trIt.next();
					for (Iterator boxIt = tr.getUnitBoxes().iterator(); boxIt
							.hasNext();) {
						final UnitBox box = (UnitBox) boxIt.next();
						if (box.getUnit() == u)
							box.setUnit(gotoStmt);
					} // each box in trap
				} // each trap
			} // if return stmt
		} // each stmt in body
		//returns.put(b, endnop);
		return (endnop);
	}


  /**
   * inline a call to this() if one exists, return a ConstructorInliningMap if
   * an inlining was done, null otherwise (no this() to inline).
   */
  public static ConstructorInliningMap inlineThisCall(SootMethod method){
        // assure body is a constructor
        if (!method.getName().equals("<init>"))
	   throw new InternalCompilerError 
	     ("trying to inline a this() in a method that is not an <init>");

	// get the body
	Body b = method.getActiveBody();

	// get the units
        Chain containerUnits = b.getUnits();

        // if the first invoke is a this() and not a super() inline the this()
        InvokeStmt invokeStmt = findInitStmt(containerUnits);

        SpecialInvokeExpr specInvokeExpr = 
	    (SpecialInvokeExpr)invokeStmt.getInvokeExpr();

        // if it is a this() call, need to do the inlining
        if (specInvokeExpr.getMethodRef().declaringClass().equals(
	                         b.getMethod().getDeclaringClass())){
            
            // put locals from inlinee into container
            SootMethod inlinee = specInvokeExpr.getMethodRef().resolve();
            if (!inlinee.hasActiveBody()){
                inlinee.retrieveActiveBody();
            }

	    Body inlineeBody=inlinee.getActiveBody();

            HashMap oldLocalsToNew = new HashMap();
            
            Iterator localsIt = inlineeBody.getLocals().iterator();
            while (localsIt.hasNext()){
                Local l = (Local)localsIt.next();
                Local newLocal = (Local)l.clone();
                b.getLocals().add(newLocal);
                oldLocalsToNew.put(l, newLocal);
            }
           
            //find identity stmt of original method
            IdentityStmt origIdStmt = findIdentityStmt(b);
           
            HashMap oldStmtsToNew = new HashMap();
            
            //System.out.println("locals: "+b.getLocals());
            Iterator inlineeIt = inlineeBody.getUnits().iterator();
            while (inlineeIt.hasNext()){
                Stmt inlineeStmt = (Stmt)inlineeIt.next();
               
                // handle identity stmts
                if (inlineeStmt instanceof IdentityStmt){
                    IdentityStmt idStmt = (IdentityStmt)inlineeStmt;
                    
                    if (idStmt.getRightOp() instanceof ThisRef) {
                        Stmt newThis = Jimple.v().newAssignStmt((Local)oldLocalsToNew.get(idStmt.getLeftOp()), origIdStmt.getLeftOp());         
                        newThis.addAllTagsOf(idStmt);
                        containerUnits.insertBefore(newThis, invokeStmt);
                        oldStmtsToNew.put(inlineeStmt, newThis);
                    }
                    
                    else if (idStmt.getRightOp() instanceof CaughtExceptionRef){
                        Stmt newInlinee = (Stmt)inlineeStmt.clone();
                        Iterator localsToPatch = newInlinee.getUseAndDefBoxes().iterator();
                        while (localsToPatch.hasNext()){
                            ValueBox next = (ValueBox)localsToPatch.next();
                            if (next.getValue() instanceof Local){
                                next.setValue((Local)oldLocalsToNew.get(next.getValue()));
                            }
                        }
                       
                        newInlinee.addAllTagsOf(inlineeStmt);
                        containerUnits.insertBefore(newInlinee, invokeStmt);
                        oldStmtsToNew.put(inlineeStmt, newInlinee);
                    }
                    else if (idStmt.getRightOp() instanceof ParameterRef) {
                        Stmt newParam = Jimple.v().newAssignStmt((Local)oldLocalsToNew.get(idStmt.getLeftOp()), specInvokeExpr.getArg(((ParameterRef)idStmt.getRightOp()).getIndex()));         
                        newParam.addAllTagsOf(idStmt);
                        containerUnits.insertBefore(newParam, invokeStmt);
                        oldStmtsToNew.put(inlineeStmt, newParam);
                    }
                }

                // handle return void stmts (cannot return anything else 
                // from a constructor)
                else if (inlineeStmt instanceof ReturnVoidStmt){
                    Stmt newRet = Jimple.v().newGotoStmt((Stmt)containerUnits.getSuccOf(invokeStmt));
                    newRet.addAllTagsOf(inlineeStmt);
                    containerUnits.insertBefore(newRet, invokeStmt);
                    debug("adding to stmt map: "+inlineeStmt+" and "+newRet);
                    oldStmtsToNew.put(inlineeStmt, newRet);
                }

                else {
                    Stmt newInlinee = (Stmt)inlineeStmt.clone();
                    Iterator localsToPatch = newInlinee.getUseAndDefBoxes().iterator();
                    while (localsToPatch.hasNext()){
                        ValueBox next = (ValueBox)localsToPatch.next();
                        if (next.getValue() instanceof Local){
                            next.setValue((Local)oldLocalsToNew.get(next.getValue()));
                        }
                    }

                    newInlinee.addAllTagsOf(inlineeStmt);   
                    containerUnits.insertBefore(newInlinee, invokeStmt);
                    oldStmtsToNew.put(inlineeStmt, newInlinee);
                }
                
            }
                
            // handleTraps
            Iterator trapsIt = inlineeBody.getTraps().iterator();
            while (trapsIt.hasNext()){
                Trap t = (Trap)trapsIt.next();
                debug("begin: "+t.getBeginUnit());
                Stmt newBegin = (Stmt)oldStmtsToNew.get(t.getBeginUnit());
                debug("end: "+t.getEndUnit());
                Stmt newEnd = (Stmt)oldStmtsToNew.get(t.getEndUnit());
                debug("handler: "+t.getHandlerUnit());
                Stmt newHandler = (Stmt)oldStmtsToNew.get(t.getHandlerUnit());

                if (newBegin == null || newEnd == null || newHandler == null)
                    throw new RuntimeException("couldn't map trap!");

                b.getTraps().add(Jimple.v().newTrap(t.getException(), newBegin, newEnd, newHandler));
            }

            // patch gotos
            inlineeIt = inlineeBody.getUnits().iterator();
            while (inlineeIt.hasNext()){
                Stmt newStmt = (Stmt) oldStmtsToNew.get(inlineeIt.next());
		Iterator unitBoxesIt=newStmt.getUnitBoxes().iterator();
		while(unitBoxesIt.hasNext()) {
		    UnitBox box=(UnitBox) unitBoxesIt.next();
		    if(oldStmtsToNew.containsKey(box.getUnit()))
			box.setUnit((Stmt) oldStmtsToNew.get(box.getUnit()));
		}
	    }
                
            // remove original invoke
            containerUnits.remove(invokeStmt);
               
            // resolve name collisions
            LocalNameStandardizer.v().transform(b, "ji.lns");

            ConstructorInliningMap cim = new ConstructorInliningMap(inlinee, method);
            cim.add(oldLocalsToNew);
            cim.add(oldStmtsToNew);

	    // return cim to indicate an inlining happened
	    return cim;
        }
	return null; // no inlining
    }

    private static IdentityStmt findIdentityStmt(Body b){
        Iterator it = b.getUnits().iterator();
        while (it.hasNext()){
            Stmt s = (Stmt)it.next();
            if ((s instanceof IdentityStmt) && (((IdentityStmt)s).getRightOp() instanceof ThisRef)){
                return (IdentityStmt)s;
            }
        }
        return null;
    }

    //private static Map/*<SootMethod,Local>*/ thiscopies=new Hashtable();

    /** Restructure the method to place a copy of 'this' in a new local
     *  variable. Why is this useful? The local bound by the relevant 
     *  identity statement will always point to local variable slot 0
     *  (ignoring ITD stuff), which will always be 'this' at method entry
     *  However, optimised bytecode might reuse slot 0 at a later point,
     *  and we want to be sure we really have a handle on the value of
     *  'this', not whatever it gets reused for later (which might even
     *  be the wrong type).
     * 
     *  UPDATE: Experiments show that the this-local never changes throughout the method,
     *  even if the byte-code assigns to slot 0. In this case, Jimple creates a new local
     *  to assign to.
     *  FIXME relational aspects will actually set the this-local to another value...
     *  (Eric)
     */
    public static Local getThisLocal(SootMethod m) {
	//if(thiscopies.containsKey(m)) return ((Local) thiscopies.get(m));
	
	Body b=m.getActiveBody();
	if (MethodCategory.hasThisAsFirstParameter(m)) {
		return b.getParameterLocal(0);
	} else {
		return b.getThisLocal();
	}
	
	/*
	
	// don't want exceptions "fixed" up
	Chain units=b.getUnits().getNonPatchingChain();
	for(Stmt stmt=(Stmt) units.getFirst(); ;
	    stmt=(Stmt) units.getSuccOf(stmt)) {
	    
	    if(stmt==null) throw new RuntimeException
			       ("internal error: didn't find identitystmt binding this "
				+"in method "+m);

	    if(stmt instanceof IdentityStmt) {
			IdentityStmt istmt=(IdentityStmt) stmt;
			
			// for ITDs "this" means the first parameter
			if (MethodCategory.hasThisAsFirstParameter(m)) {
				if (istmt.getRightOp() instanceof ParameterRef) {
					ParameterRef parref = (ParameterRef) istmt.getRightOp();
					if (parref.getIndex()==0) {
					   Value tr=istmt.getLeftOp();
					   do { 
					   stmt=(Stmt) units.getSuccOf(stmt); 
					   } while(stmt instanceof IdentityStmt);
					   units.insertBefore(Jimple.v().newAssignStmt(l,tr),stmt);
					   break;
					}
				}
			} else 
			// the normal non-ITD case
			if(istmt.getRightOp() instanceof ThisRef) {
			    Value tr=istmt.getLeftOp();
			    do { 
				stmt=(Stmt) units.getSuccOf(stmt); 
			    } while(stmt instanceof IdentityStmt);
			    units.insertBefore(Jimple.v().newAssignStmt(l,tr),stmt);
			    break;
			}
	    }
	}
	thiscopies.put(m,l);
	return l;*/
    }

    private static Map/*<InvokeStmt,AssignStmt>*/ invokeassignstmts=new Hashtable();
    
    public static AssignStmt getEquivAssignStmt(SootMethod m,InvokeStmt stmt) {
	// We assume that a statement will only ever occur in one method
	// If this is not true, then use a separate table to check whether
	// the assignstmt has been repatched into a particular method

	if(invokeassignstmts.containsKey(stmt)) 
	    return ((AssignStmt) (invokeassignstmts.get(stmt)));

	Body b=m.getActiveBody();
	Chain units=b.getUnits();
	InvokeExpr e=stmt.getInvokeExpr();
	Local l=new LocalGeneratorEx(b).generateLocal(e.getMethodRef().returnType(),"retval");
	AssignStmt a=Jimple.v().newAssignStmt(l,e);

	units.swapWith(stmt,a);
	if(stmt.hasTag("SourceLnPosTag")) a.addTag(stmt.getTag("SourceLnPosTag"));
	invokeassignstmts.put(stmt,a);
	return a;
    }
	
	/**
	 * Sanity check for methods.
	 * Checks for:
	 * 	- @this identity statement as the first statement
	 *  - parameter identity statements in the right order for all parameters
	 * @param method
	 */
	public static void validateMethod(SootMethod method) {
		debug("validating " + method.getName());
		
		if (method.isAbstract())
			return;
		
		Body body=method.getActiveBody();
		Chain units=body.getUnits().getNonPatchingChain();
		List params=method.getParameterTypes();
		
		Iterator itUnits=units.iterator();
		if (!method.isStatic()) {
			Stmt first=(Stmt)itUnits.next();		
			
			IdentityStmt id=(IdentityStmt) first;
			Local local=(Local)id.getLeftOp();
			ThisRef ref=(ThisRef)id.getRightOp();
			if (!ref.getType().equals(method.getDeclaringClass().getType()))
				throw new RuntimeException();
			
			if (!local.getType().equals(method.getDeclaringClass().getType()))
				throw new RuntimeException();
			
		}	
		
		Iterator it=params.iterator();
		int i=0;
		while (it.hasNext()) {
			Type type=(Type)it.next();
			Stmt stmt=(Stmt)itUnits.next();
			IdentityStmt id=(IdentityStmt)stmt;
			Local local=(Local)id.getLeftOp();
			
			debug("  parameter " + i + ": " + type.toString() + ":" + local.getName());
			debug("   rightOp: " + id.getRightOp());
			ParameterRef ref=(ParameterRef)id.getRightOp();
		
					
			
			if (!Type.toMachineType(local.getType()).equals(Type.toMachineType(type))) {
				debug("type mismatch: local: " + local.getType() + " param: " + type);
				throw new RuntimeException();
			}
			if (ref.getIndex()!=i++) {
				throw new RuntimeException();
			}
			if (!ref.getType().equals(type)) {
				throw new RuntimeException();
			}				
		}
		
	}

	/**
	 * This class helps implement boxing.
	 * It assigns integer IDs to the simple Java types and
	 * one ID to all the reference types.
	 * 
	 * It contains methods to retrieve the boxing classes of simple types
	 * and their value methods (intVal() etc.) 
	 * 
	 * @author Sascha Kuzins 
	 */
	public static class JavaTypeInfo {
		public final static int booleanType=0;
		public final static int byteType=1;
		public final static int shortType=2;
		public final static int charType=3;
		public final static int intType=4;
		public final static int longType=5;
		public final static int floatType=6;
		public final static int doubleType=7;
		public final static int refType=8;
		public final static int typeCount=9;
		
		/*
		 * This table is from Java in a Nutshell 3rd edition, page 28
		 * X - same type
		 * N - forbidden conversion
		 * Y - widening conversion
		 * C - narrowing conversion
		 */
		private final static char [][] simple_conversions =
			{ 
				{ 'X', 'N', 'N', 'N', 'N', 'N', 'N', 'N'},
				{ 'N', 'X', 'Y', 'C', 'Y', 'Y', 'Y', 'Y'},
				{ 'N', 'C', 'X', 'C', 'Y', 'Y', 'Y', 'Y'},
				{ 'N', 'C', 'C', 'X', 'Y', 'Y', 'Y', 'Y'},
				{ 'N', 'C', 'C', 'C', 'X', 'Y', 'Y', 'Y'},
				{ 'N', 'C', 'C', 'C', 'C', 'X', 'Y', 'Y'},
				{ 'N', 'C', 'C', 'C', 'C', 'C', 'X', 'Y'},
				{ 'N', 'C', 'C', 'C', 'C', 'C', 'C', 'X'}
			};
		public static char getSimpleTypeConversionInfo(Type from, Type to) {
			int f=sootTypeToInt(from);
			if (!isSimpleType(f))
				throw new RuntimeException();
			int t=sootTypeToInt(to);
			if (!isSimpleType(t))
				throw new RuntimeException();
			
			//if (simple_conversions[longType][intType]!='C')
			//	throw new RuntimeException();
			
			return simple_conversions[f][t];
		}
		
		public static boolean isSimpleType(Type t) {
			return isSimpleType(sootTypeToInt(t));
		}
		
		public static boolean isSimpleType(int type) {
			return type!=refType;
		}
		public static boolean isSimpleWideningConversion(Type from, Type to) {
			char c=getSimpleTypeConversionInfo(from, to);
			return c=='Y' || c=='X'; 
		}
		// returns if the classes contain a method with the same name and signature 
		// but different return type
		public static boolean haveCollidingMethod(SootClass cl, SootClass cl2) {
			for( Iterator mIt = cl.getMethods().iterator(); mIt.hasNext(); ) {
			    final SootMethod m = (SootMethod) mIt.next();
				try {
					
					SootMethod m2=cl2.getMethod(m.getName(), m.getParameterTypes());
					if (!m2.getReturnType().equals(m.getReturnType()))
						return true;
					
				} catch (RuntimeException e) {
					if (e.getMessage().matches("ambiguous method"))
						throw new RuntimeException("Internal error, unexpected exception.");
				}
			}
			return false;
		}
		
		public static boolean implementsInterfaceRecursive(SootClass cl, String interfaceName)  {
			if (cl.implementsInterface(interfaceName))
				return true;
			else {
				if (cl.hasSuperclass())
					return implementsInterfaceRecursive(cl.getSuperclass(), interfaceName);
				else
					return false;
			}
		}
		/*
		 * The aim is to return true for cases which javac would 
		 * identify as an invalid cast at compile time.
		 * (For example boolean to int, or unrelated RefTypes).
		 * Based on 5.1.7 of the Java Language Spec, second edition, page 64.
		 * Also see 5.5, Casting Conversion. 
		 * The #numbers in the code indicate the bullet point to which the test 
		 * refers.
		 */
		public static boolean isForbiddenConversion(Type from, Type to) {
			// AnySubType is only used for points-to analysis
			if (from instanceof AnySubType) throw new RuntimeException();
			if (to instanceof AnySubType) throw new RuntimeException();
			
			// #4
			if (to instanceof NullType && !(from instanceof NullType ))
				return true;			
			
			if (to.equals(from))
				return false;
			
			if (from instanceof NullType && to instanceof RefLikeType)
				return false;	
			
			// #3
			if (from instanceof NullType && isSimpleType(to))
				return true;
			
			if (isSimpleType(from) && isSimpleType(to))
				return isForbiddenSimpleConversion(from, to); // #5,6
			
			
			// At this stage, they are not both simple types.			
			// #1, #2
			if (isSimpleType(from) || isSimpleType(to))
				return true;
			
			if (!(from instanceof RefType && to instanceof RefType)) {
				// not both are reftypes
				
				// #12,13
				if (from instanceof ArrayType && to instanceof RefType &&
					! (		to.equals(RefType.v("java.lang.Object")) || 
							to.equals(RefType.v("java.io.Serializable")) ||
							to.equals(RefType.v("java.lang.Cloneable")) )) 
					return true; // cast from array to non-object reftype
				
				// #9
				if (to instanceof ArrayType && from instanceof RefType &&
						!from.equals(RefType.v("java.lang.Object"))) 
					return true; // cast from non-object reftype to array
				
				
				// #14
				if (to instanceof ArrayType && from instanceof ArrayType) {
				 	ArrayType ato=(ArrayType)to;
				 	ArrayType afrom=(ArrayType)from;
				 	return 
						isForbiddenConversion(afrom.baseType, ato.baseType);				 		 
				}				
				
				return false;
			}
			
			RefType rFrom=(RefType)from;
			RefType rTo=(RefType)to;
			
			// #11
			if (rFrom.getSootClass().isInterface() && rTo.getSootClass().isInterface()) {
				if (haveCollidingMethod(rFrom.getSootClass(), rTo.getSootClass()))
					return true;
				else
					return false;
			}
			// #8
			if (rTo.getSootClass().isInterface())  {
				if (Modifier.isFinal(rFrom.getSootClass().getModifiers()) &&
					implementsInterfaceRecursive(rFrom.getSootClass(), rTo.getSootClass().getName()))
					return true;
				else
					return false; // from could implement this interface
			}
			// #10
			if (rFrom.getSootClass().isInterface()) {  
				if (Modifier.isFinal(rTo.getSootClass().getModifiers()) &&
						implementsInterfaceRecursive(rTo.getSootClass(), rFrom.getSootClass().getName()))
					return true;
				else						// from can implement other interfaces and
					return false;			// a non-interface type, so cast could be possible
			}
			
			
			
			FastHierarchy hier=Scene.v().getOrMakeFastHierarchy();
			// #7
			if (hier.isSubclass(rFrom.getSootClass(), rTo.getSootClass()) ||
				hier.isSubclass(rTo.getSootClass(), rFrom.getSootClass())) 
				return false;
			else
				return true;
		}
		public static boolean isForbiddenSimpleConversion(Type from, Type to) {
			char c=getSimpleTypeConversionInfo(from, to);
			return c=='N';
		}
		
		public static int sootTypeToInt(Type type) {
			if (type.equals(IntType.v()))
				return intType;
			else if (type.equals(BooleanType.v())) 
				return booleanType;
			else if (type.equals(ByteType.v())) 
				return byteType;
			else if (type.equals(ShortType.v())) 
							return shortType;
			else if (type.equals(CharType.v())) 
							return charType;
			else if (type.equals(LongType.v())) 
							return longType;
			else if (type.equals(FloatType.v())) 
							return floatType;
			else if (type.equals(DoubleType.v())) 
							return doubleType;
			else 
				return refType;
		}
		public static Value getDefaultValue(Type type) {
			if (type.equals(IntType.v()))
				return IntConstant.v(0);
			else if (type.equals(BooleanType.v())) 
				return IntConstant.v(0); 
			else if (type.equals(ByteType.v())) 
				return IntConstant.v(0); ///
			else if (type.equals(ShortType.v())) 
				return IntConstant.v(0); ///
			else if (type.equals(CharType.v())) 
				return IntConstant.v(0); ///
			else if (type.equals(LongType.v())) 
				return LongConstant.v(0);
			else if (type.equals(FloatType.v())) 
				return FloatConstant.v(0.0f);
			else if (type.equals(DoubleType.v())) 
				return DoubleConstant.v(0.0);
			else 
				return NullConstant.v();
		}
		public static SootClass getBoxingClass(Type type) {
			if (type.equals(IntType.v()))
				return Scene.v().getSootClass("java.lang.Integer");
			else if (type.equals(BooleanType.v())) 
				return Scene.v().getSootClass("java.lang.Boolean");
			else if (type.equals(ByteType.v())) 
				return Scene.v().getSootClass("java.lang.Byte");
			else if (type.equals(ShortType.v())) 
				return Scene.v().getSootClass("java.lang.Short");
			else if (type.equals(CharType.v())) 
				return Scene.v().getSootClass("java.lang.Character");
			else if (type.equals(LongType.v())) 
				return Scene.v().getSootClass("java.lang.Long");
			else if (type.equals(FloatType.v())) 
				return Scene.v().getSootClass("java.lang.Float");
			else if (type.equals(DoubleType.v())) 
				return Scene.v().getSootClass("java.lang.Double");
			else 
				throw new RuntimeException();
		}

		public static String getBoxingClassMethodName(Type type) {
			return getSimpleTypeBoxingClassMethodName(
					getBoxingClassPrimType(((RefType)type).getSootClass()));
		}
		public static String getSimpleTypeBoxingClassMethodName(Type type) {	
			if (type.equals(IntType.v()))
				return "intValue";
			else if (type.equals(BooleanType.v())) 
				return "booleanValue";
			else if (type.equals(ByteType.v())) 
				return "byteValue";
			else if (type.equals(ShortType.v())) 
				return "shortValue";
			else if (type.equals(CharType.v())) 
				return "charValue";
			else if (type.equals(LongType.v())) 
				return "longValue";
			else if (type.equals(FloatType.v())) 
				return "floatValue";
			else if (type.equals(DoubleType.v())) 
				return "doubleValue";
			else 
				throw new RuntimeException("no method for type " + type);
		}
		
		/** Given the boxing class, what was the primitive type?
		 * 
		 * @param boxingClass the boxing class (eg Integer)
		 * @return the primitive type (eg int)
		 */
		public static Type getBoxingClassPrimType(SootClass boxingClass) {
			if (boxingClass.equals(getBoxingClass(IntType.v())))
				return IntType.v();
			else if (boxingClass.equals(getBoxingClass(BooleanType.v()))) 
				return BooleanType.v();
			else if (boxingClass.equals(getBoxingClass(ByteType.v()))) 
				return ByteType.v();
			else if (boxingClass.equals(getBoxingClass(ShortType.v()))) 
				return ShortType.v();
			else if (boxingClass.equals(getBoxingClass(CharType.v()))) 
				return CharType.v();
			else if (boxingClass.equals(getBoxingClass(LongType.v()))) 
				return LongType.v();
			else if (boxingClass.equals(getBoxingClass(FloatType.v())))
				return FloatType.v();
			else if (boxingClass.equals(getBoxingClass(DoubleType.v()))) 
				return DoubleType.v();
			else 
				throw new RuntimeException();
		}
		public static boolean isBoxingType(Type type) {
			if (type instanceof RefType) {
				RefType rt=(RefType)type;
				try {
					getBoxingClassPrimType(rt.getSootClass());
					return true;
				} catch(RuntimeException e) {
					return false;
				}
			} else
				return false;
		}
	}
	/**
	 * Converts the assignment statement into a sequence 
	 * of statements performing a typecast.
	 * Boxes/unboxes if the assignment is from/to an Object to/from a simple type. 
	 * @param body
	 * @param stmt
	 */
	public static boolean insertBoxingCast(Body body, AssignStmt stmt, boolean allowBoxing) {
		boolean bDidUnBox=false;
		ValueBox source=stmt.getRightOpBox();
		Value targetVal=stmt.getLeftOp();
		Type targetType=stmt.getLeftOp().getType();
		Chain units=body.getUnits().getNonPatchingChain();
		Type sourceType=source.getValue().getType();
		if (!sourceType.equals(targetType) && 
			  !(sourceType instanceof RefType &&
				targetType instanceof RefType &&
				isBaseClass(((RefType)targetType).getSootClass(), 
							((RefType)sourceType).getSootClass()))) {
				
			
			
			LocalGeneratorEx localgen=new LocalGeneratorEx(body);
			Local castLocal=localgen.generateLocal(sourceType, "castTmp");
			debug("cast: source has type " + sourceType.toString());
			debug("cast: target has type " + targetType.toString());
			stmt.setLeftOp(castLocal);
			
			AssignStmt tmpStmt=Jimple.v().newAssignStmt(targetVal, targetVal /*dummy*/);
			units.insertAfter(tmpStmt, stmt);
						
			Value castedExpr;
			//debug("boxing: source " + sourceType + " target " + targetType);
			// boxing
			if (allowBoxing && JavaTypeInfo.sootTypeToInt(sourceType)!=JavaTypeInfo.refType &&
				targetType.equals(Scene.v().getSootClass("java.lang.Object").getType())) {
				SootClass boxClass=JavaTypeInfo.getBoxingClass(sourceType);	
				 Local box=localgen.generateLocal(boxClass.getType(), "box");
				 Stmt newAssignStmt = Jimple.v().newAssignStmt( box, Jimple.v().newNewExpr( boxClass.getType() ) );
				 List initParams=new LinkedList();
				 initParams.add(sourceType);
				 Stmt initBox=Jimple.v().newInvokeStmt( 
				 	Jimple.v().newSpecialInvokeExpr( box, Scene.v().makeConstructorRef(boxClass,initParams), 
				 			castLocal)) ;
				units.insertBefore(newAssignStmt, tmpStmt);
				units.insertBefore(initBox, tmpStmt);
				castedExpr=box;
				bDidUnBox=true;
			} else if /*unboxing*/
				(allowBoxing && JavaTypeInfo.sootTypeToInt(targetType)!=JavaTypeInfo.refType &&
					sourceType.equals(Scene.v().getSootClass("java.lang.Object").getType())	){ 
				SootClass boxClass=JavaTypeInfo.getBoxingClass(targetType);	
				Local box=localgen.generateLocal(boxClass.getType(), "box");
				Stmt newAssignStmt=Jimple.v().newAssignStmt(box, 
					Jimple.v().newCastExpr(castLocal, boxClass.getType()));
				SootMethodRef method=Scene.v().makeMethodRef
				    (boxClass,
				     JavaTypeInfo.getSimpleTypeBoxingClassMethodName(targetType),
				     new ArrayList(),
				     targetType,
				     false);
				castedExpr=Jimple.v().newVirtualInvokeExpr(box, 
						 method);		
				units.insertBefore(newAssignStmt, tmpStmt);	
				bDidUnBox=true;
			} else { // normal cast
				CastExpr castExpr=Jimple.v().newCastExpr(castLocal,targetType);
				castedExpr=castExpr;	
			}
			
			tmpStmt.setRightOp(castedExpr);
		//	Jimple.v().newCastExpr()
			/*
			if (stmt instanceof AssignStmt) {
				source.setValue(castedExpr);
			} else {
				Local tmpLocal=localgen.generateLocal(targetType, "castTarget");
				AssignStmt tmpStmt2=Jimple.v().newAssignStmt(tmpLocal, castedExpr);
				units.insertBefore(tmpStmt2, stmt);
				source.setValue(tmpLocal);
			}*/
		} 		
		return bDidUnBox;
	}
	/**
	 * Retrieves the identity statement of the argument at position arg
	 * @param method
	 * @param arg
	 * @return
	 */
	public static IdentityStmt getParameterIdentityStatement(SootMethod method, int arg) {
		if (arg>=method.getParameterCount())
			throw new RuntimeException();
		Chain units=method.getActiveBody().getUnits().getNonPatchingChain();
		Iterator it=units.iterator();
		while (it.hasNext()) {
			Stmt stmt=(Stmt)it.next();
			if (stmt instanceof IdentityStmt) {
				IdentityStmt ids=(IdentityStmt)stmt;
				if (ids.getRightOp() instanceof ParameterRef) {
					ParameterRef paramRef=(ParameterRef)ids.getRightOp();
					if (paramRef.getIndex()==arg)
						return ids;
					
				} else if (ids.getRightOp() instanceof ThisRef) {
					
				} else 
					throw new RuntimeException();
			} else
				throw new RuntimeException();
		}
		throw new RuntimeException();
	}
	/**
	 * Adds a new parameter to a method and creates the matching identity statement.
	 * Returns the local of the newly created parameter.
	 * @param method
	 * @param type
	 * @param suggestedName
	 * @return
	 */
	public static Local addParameterToMethod(SootMethod method, Type type, String suggestedName) {
		//validateMethod(method);
		Body body=method.getActiveBody();
		Chain units=body.getUnits().getNonPatchingChain();
		List params=new LinkedList(method.getParameterTypes());
		
		IdentityStmt lastIDStmt=null;
		if (params.isEmpty()) {
			if (units.isEmpty()) {
				if (!method.isStatic())
					throw new RuntimeException();
			} else {
				lastIDStmt=(IdentityStmt)units.getFirst();
				if (! (lastIDStmt.getRightOp() instanceof ThisRef))
					if (!method.isStatic())
						throw new RuntimeException();
			}
		} else {
		//	debug("param id: " + (params.size()-1));
			lastIDStmt=Restructure.getParameterIdentityStatement(method, params.size()-1);
		}
		params.add(type);
		method.setParameterTypes(params);
		LocalGeneratorEx lg=new LocalGeneratorEx(body);
		Local l=lg.generateLocal(type, suggestedName);
		IdentityStmt newIDStmt=Jimple.v().newIdentityStmt(l, 
			Jimple.v().newParameterRef(type, params.size()-1));
		if (lastIDStmt==null)
			units.addFirst(newIDStmt);
		else
			units.insertAfter(newIDStmt, lastIDStmt);
		return l;		
	}

	public static boolean isBaseClass(SootClass baseClass, SootClass subClass) {
		SootClass sub = subClass;
	
		while (sub.hasSuperclass()) {
			SootClass superClass = sub.getSuperclass();
			if (superClass.equals(baseClass))
				return true;
	
			sub = superClass;
		}
		return false;
	}	
	

} // class Restructure
