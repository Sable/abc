package abc.soot.util;

import soot.*;
import soot.util.*;
import soot.jimple.*;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.toolkits.scalar.*;
import java.util.*;
import abc.weaving.weaver.CodeGenException;

/** This class contains a variety of helper methods to restructure Soot
 *    method Bodies.
 *
 * @author Laurie Hendren
 * @author Ondrej Lhotak
 * @author Jennifer Lhotak
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

  /** Given a SootMethod, restructure its body so that the body ends
   *  with   L1:nop; return;    or   L1:nop; return(<local>);.
   *  Rewire all other returns in the body to assign to <local> and
   *  goto L1.   Return a reference to the nop at L1.
   */
  public static Stmt restructureReturn(SootMethod method) {
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
                       
                        containerUnits.insertBefore(newInlinee, invokeStmt);
                        oldStmtsToNew.put(inlineeStmt, newInlinee);
                    }
                    else if (idStmt.getRightOp() instanceof ParameterRef) {
                        Stmt newParam = Jimple.v().newAssignStmt((Local)oldLocalsToNew.get(idStmt.getLeftOp()), specInvokeExpr.getArg(((ParameterRef)idStmt.getRightOp()).getIndex()));         
                        containerUnits.insertBefore(newParam, invokeStmt);
                        oldStmtsToNew.put(inlineeStmt, newParam);
                    }
                }

                // handle return void stmts (cannot return anything else 
                // from a constructor)
                else if (inlineeStmt instanceof ReturnVoidStmt){
                    Stmt newRet = Jimple.v().newGotoStmt((Stmt)containerUnits.getSuccOf(invokeStmt));
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
} // class Restructure
