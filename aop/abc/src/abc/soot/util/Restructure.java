package abc.soot.util;

import soot.*;
import soot.util.*;
import soot.jimple.*;
import soot.javaToJimple.LocalGenerator;
import abc.weaving.weaver.AroundWeaver;
import soot.jimple.toolkits.scalar.*;
import java.util.*;

import abc.weaving.weaver.CodeGenException;



/** This class contains a variety of help
import abc.weaving.weaver.AroundWeaver.InternalError;
er methods to restructure Soot
 *    method Bodies.
 *
 * @author Laurie Hendren
 * @author Ondrej Lhotak
 * @author Jennifer Lhotak
 * @author Sascha Kuzins
 * @date May 18, 2004
 */

public class Restructure {

   private static void debug(String message)
     { if (abc.main.Debug.v().restructure)
         System.err.println("RST*** " + message);
     }

   /* ------------------- Utility restructurers ----------------------- */

  /** Given a Chain for a body of an <init> method, find the call to
   *  the <init> corresponding to either a this() or super() call.  Check
   *  that there is exactly one such <init>, otherwise throw a 
   *  CodeGenException.   Return the <init> Stmt.
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
	  throw new CodeGenException("Expecting an identity stmt for this");
	
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
	     receivercopies.contains(
	       ((SpecialInvokeExpr) ((InvokeStmt) u).getInvokeExpr()).
	                                                      getBase()))
	    { debug("Found <init> " + u);
	      countinits++;
	      if (countinits == 1) // great, found it
	        initstmt = (InvokeStmt) u;  
	      else
	        throw new CodeGenException("Expecting only one <init>");
             }	 
          } // all units
	 
       debug("--- Finished looking through statement list ..... ");
       if (countinits == 0)     
         throw new CodeGenException("Could not find a matching <init>");

       return(initstmt);
     }

   /** Given a Chain for the body of a method,  find the first "real"
    *  stmt (i.e. not an identity stmt) and return a reference to it.
    */
   public static Stmt findFirstRealStmt(Chain units)
     { Iterator it = units.snapshotIterator();
       while ( it.hasNext() )
         { Stmt u = (Stmt) it.next();
           if (! (u instanceof IdentityStmt)) // first non-IdentityStmt
             return(u);
          }
        throw new CodeGenException("Expecting to find a real stmt");
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

  private static Map/*<SootMethod,Stmt>*/ returns=new Hashtable();

  /** Given a SootMethod, restructure its body so that the body ends
   *  with   L1:nop; return;    or   L1:nop; return(<local>);.
   *  Rewire all other returns in the body to assign to <local> and
   *  goto L1.   Return a reference to the nop at L1.
   */
  public static Stmt restructureReturn(SootMethod method) {
    if(returns.containsKey(method)) return ((Stmt) returns.get(method));
    Body b = method.getActiveBody();
    LocalGenerator localgen = new LocalGenerator(b);
    Stmt endnop = Jimple.v().newNopStmt();
    Chain units = b.getUnits();  // want a patching chain here, to make sure
                             // gotos are rewired to go to the inserted nop
    Stmt last = (Stmt) units.getLast();

    Local ret = null;
    if( last instanceof ReturnStmt ) 
      { ReturnStmt lastRet = (ReturnStmt) last;
	Value op = lastRet.getOp();
        if (op instanceof Local) // return(<local>)
          ret = (Local) op; // remember this local
	else if (op instanceof Constant) // return(<constant>)
	  { // change to ret := <constant>; return(ret); 
	    Type returnType = method.getReturnType();
	    ret = localgen.generateLocal(returnType);
	    units.insertBefore(Jimple.v().newAssignStmt(ret,op),lastRet); 
            lastRet.setOp(ret);
	  }
	else
	  throw new CodeGenException(
	                  "Expecting return of <local> or <constant>");
      } 
    else if( last instanceof ReturnVoidStmt ) 
      { // do nothing 
      } 
    else 
      { Type returnType = method.getReturnType();
        ret = localgen.generateLocal(returnType);
        units.insertAfter( Jimple.v().newReturnStmt(ret), last );
      }

    // now the last stmt should always be return ret; or return;
    if( units.getLast() instanceof ReturnStmt ) 
      { ReturnStmt lastRet = (ReturnStmt) units.getLast();
        if( lastRet.getOp() != ret ) 
           throw new CodeGenException("Expecting last stmt to Return ret");
      }  
    else if ( !(units.getLast() instanceof ReturnVoidStmt ) ) 
      { throw new CodeGenException(
	           "Last stmt should be ReturnStmt or ReturnVoidStmt");
      }

    // insert the nop just before the return stmt
    units.insertBefore( endnop, units.getLast() );

    resetTrapsEnd(b,(Stmt) units.getLast(),endnop);

    // Look for returns in the middle of the method
    Iterator it = units.snapshotIterator();
    while( it.hasNext() ) 
      { Stmt u = (Stmt) it.next();
        if( u == units.getLast() ) continue;
        if( u instanceof ReturnStmt ) 
	  { ReturnStmt ur = (ReturnStmt) u;
            units.insertBefore( 
		Jimple.v().newAssignStmt( ret, ur.getOp() ), ur );
           }
        if( u instanceof ReturnVoidStmt  || u instanceof ReturnStmt) 
	  { Stmt gotoStmt = Jimple.v().newGotoStmt(endnop);
            units.swapWith( u, gotoStmt );
            for( Iterator trIt = b.getTraps().iterator(); trIt.hasNext(); ) 
	      { final Trap tr = (Trap) trIt.next();
                for( Iterator boxIt = tr.getUnitBoxes().iterator(); 
		    boxIt.hasNext(); ) 
		  { final UnitBox box = (UnitBox) boxIt.next();
                    if( box.getUnit() == u ) 
                      box.setUnit( gotoStmt );
                  } // each box in trap 
              } // each trap
	  } // if return stmt
      } // each stmt in body
    returns.put(method,endnop);
    return(endnop);
  }


  /** inline a call to this() if one exists, return true if an inlining
   *  was done, false otherwise (no this() to inline).
   */
  public static boolean inlineThisCall(SootMethod method){
        // assure body is a constructor
        if (!method.getName().equals("<init>"))
	   throw new CodeGenException 
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
        if (specInvokeExpr.getMethod().getDeclaringClass().equals(
	                         b.getMethod().getDeclaringClass())){
            
            // put locals from inlinee into container
            if (!specInvokeExpr.getMethod().hasActiveBody()){
                specInvokeExpr.getMethod().retrieveActiveBody();
            }

            HashMap oldLocalsToNew = new HashMap();
            
            Iterator localsIt = specInvokeExpr.getMethod().getActiveBody().getLocals().iterator();
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
            Iterator inlineeIt = specInvokeExpr.getMethod().getActiveBody().getUnits().iterator();
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
            Iterator trapsIt = specInvokeExpr.getMethod().getActiveBody().getTraps().iterator();
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
            inlineeIt = specInvokeExpr.getMethod().getActiveBody().getUnits().iterator();
            while (inlineeIt.hasNext()){
                Stmt inlineeStmt = (Stmt)inlineeIt.next();
                if (inlineeStmt instanceof GotoStmt){
                    debug("inlinee goto target: "+((GotoStmt)inlineeStmt).getTarget());
                    ((GotoStmt)oldStmtsToNew.get(inlineeStmt)).setTarget((Stmt)oldStmtsToNew.get(((GotoStmt)inlineeStmt).getTarget()));
                }
                else if (inlineeStmt instanceof IfStmt){
                    ((IfStmt)oldStmtsToNew.get(inlineeStmt)).setTarget((Stmt)oldStmtsToNew.get(((IfStmt)inlineeStmt).getTarget()));
                }
                       
            }
                
            // remove original invoke
            containerUnits.remove(invokeStmt);
               
            // resolve name collisions
            LocalNameStandardizer.v().transform(b, "ji.lns");

	    // return true to indicate an inlining happened
	    return(true);
        }
	return(false); // no inlining
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

    private static Map/*<SootMethod,Local>*/ thiscopies=new Hashtable();

    public static Local getThisCopy(SootMethod m) {
	if(thiscopies.containsKey(m)) return ((Local) thiscopies.get(m));
	
	Body b=m.getActiveBody();
	Local l=new LocalGeneratorEx(b).generateLocal
	    (m.getDeclaringClass().getType(),"thisCopy");

	Chain units=b.getUnits();
	for(Stmt stmt=(Stmt) units.getFirst();
	    ;
	    stmt=(Stmt) units.getSuccOf(stmt)) {
	    
	    if(stmt==null) throw new RuntimeException
			       ("internal error: didn't find identitystmt binding this "
				+"in method "+m);

	    if(stmt instanceof IdentityStmt) {
		IdentityStmt istmt=(IdentityStmt) stmt;
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
	return l;
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
			ParameterRef ref=(ParameterRef)id.getRightOp();
		
			debug("  parameter " + i + ": " + type.toString() + ":" + local.getName());		
			
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
				throw new RuntimeException();
		}
	}

	/**
	 * Converts the assignment statement into a sequence 
	 * of statements performing a typecast.
	 * Boxes/unboxes if the assignment is from/to an Object to/from a simple type. 
	 * @param body
	 * @param stmt
	 */
	public static void insertBoxingCast(Body body, AssignStmt stmt) {
		ValueBox source=stmt.getRightOpBox();
		Value targetVal=stmt.getLeftOp();
		Type targetType=stmt.getLeftOp().getType();
		Chain units=body.getUnits().getNonPatchingChain();
		Type sourceType=source.getValue().getType();
		if (!sourceType.equals(targetType)) {
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
			if (JavaTypeInfo.sootTypeToInt(sourceType)!=JavaTypeInfo.refType &&
				targetType.equals(Scene.v().getSootClass("java.lang.Object").getType())) {
				SootClass boxClass=JavaTypeInfo.getBoxingClass(sourceType);	
				 Local box=localgen.generateLocal(boxClass.getType(), "box");
				 Stmt newAssignStmt = Jimple.v().newAssignStmt( box, Jimple.v().newNewExpr( boxClass.getType() ) );
				 List initParams=new LinkedList();
				 initParams.add(sourceType);
				 Stmt initBox=Jimple.v().newInvokeStmt( 
				 	Jimple.v().newSpecialInvokeExpr( box, boxClass.getMethod( "<init>", initParams), 
				 			castLocal)) ;
				units.insertBefore(newAssignStmt, tmpStmt);
				units.insertBefore(initBox, tmpStmt);
				castedExpr=box;
			} else if /*unboxing*/
				(JavaTypeInfo.sootTypeToInt(targetType)!=JavaTypeInfo.refType &&
					sourceType.equals(Scene.v().getSootClass("java.lang.Object").getType())	){ 
				SootClass boxClass=JavaTypeInfo.getBoxingClass(targetType);	
				Local box=localgen.generateLocal(boxClass.getType(), "box");
				Stmt newAssignStmt=Jimple.v().newAssignStmt(box, 
					Jimple.v().newCastExpr(castLocal, boxClass.getType()));
				SootMethod method=boxClass.getMethodByName(
					JavaTypeInfo.getBoxingClassMethodName(targetType));
				castedExpr=Jimple.v().newVirtualInvokeExpr(box, 
						 method);		
				units.insertBefore(newAssignStmt, tmpStmt);						
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
	}

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
	 * Adds a new parameter to a method and creates the matching identity statement
	 * @param method
	 * @param type
	 * @param suggestedName
	 * @return
	 */
	public static Local addParameterToMethod(SootMethod method, Type type, String suggestedName) {
		//validateMethod(method);
		Body body=method.getActiveBody();
		Chain units=body.getUnits().getNonPatchingChain();
		List params=method.getParameterTypes();
		
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
	

} // class Restructure
