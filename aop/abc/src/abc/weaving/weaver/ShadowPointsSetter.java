package abc.weaving.weaver;

import soot.*;
import soot.util.*;
import soot.jimple.*;
import soot.javaToJimple.LocalGenerator;
import java.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;
import abc.weaving.weaver.*;

/** The purpose of this class is to iterate over all AdviceApplication
 *    instances and initialize their ShadowPoints.   
 *    This is done within a class, for each method/constructor body.
 *    If more than one AdviceApplication applies to a particular joinpoint,
 *    then only one ShadowPoints object is allocated, that is shared between
 *    all of them.
 *
 *    Creating the ShadowPoints will also introduce new NOP statements that
 *    mark the beginning and end of the joinpoint shadow.   The weaver uses
 *    these markers to see where to insert code.
 *
 * @author Laurie Hendren
 * @author Ondrej Lhotak
 * @author Jennifer Lhotak
 * @date May 3, 2004
 */

public class ShadowPointsSetter {

    /** set to false to disable debugging messages for ShadowPointsSetter */
    public static boolean debug = true;

    private static void debug(String message)
      { if (debug) System.err.println("SPS*** " + message);
      }	

    /** Set all ShadowPoints for AdviceApplications in class sc. */

    /* --------------------------- PASS 1 --------------------------*/

    public void setShadowPointsPass1(SootClass sc) {
      debug("--- BEGIN Setting ShadowPoints Pass1 for class " + 
	  sc.getName());

      // for each method in the class 
      for( Iterator methodIt = sc.getMethods().iterator(); 
	   methodIt.hasNext(); ) {

	 // get the next method
         final SootMethod method = (SootMethod) methodIt.next();

	 // nothing to do for abstract or native methods 
         if( method.isAbstract() ) continue;
         if( method.isNative() ) continue;

	 // get all the advice list for this method
         MethodAdviceList adviceList = 
	     GlobalAspectInfo.v().getAdviceList(method);

	 // if no advice list for this method, nothing to do
	 if ((adviceList == null) || adviceList.isEmpty())
           { debug("No advice list for method " + method.getName());
	     continue;
	   }

         debug("   --- BEGIN Setting ShadowPoints Pass1 for method " + 
	                method.getName());

	 // ---- we have some advice, so set things up for this method
	 debug("Advice for method " + method.getName() + " is : \n" +
	               adviceList);

	 // --- First deal with execution pointcuts
	 if (adviceList.hasBodyAdvice())
	    insertBodySP(method,adviceList);

	 // ---- Then deal with stmt pointcuts that are in the body
         if (adviceList.hasStmtAdvice())
	    insertStmtSP(method,adviceList.stmtAdvice);

	 debug("   --- END Setting ShadowPoints Pass1 for method " + 
	                    method.getName() + "\n");
       } // for each method

      debug(" --- END Setting ShadowPoints Pass1 for class " + sc.
	          getName() + "\n");
    } // setShadowPointsPass1


  private void insertBodySP(SootMethod method, 
                      MethodAdviceList methodadvicelist) {

    debug("Need to transform for execution in method: " + method.getName()); 
    List /*<AdviceApplication>*/ advicelist = methodadvicelist.bodyAdvice; 

    // restructure returns, and insert begin and end nops
    ShadowPoints execution_sp = restructureBody(method); 

    // register that this method has already been restructured
    methodadvicelist.restructuringDone();
    debug("ShadowPoints are: " + execution_sp);
	     
    // make all execution AdviceApplications refer to the shadowpoints
    // object just constructed.
     for (Iterator alistIt = advicelist.iterator(); alistIt.hasNext();)
        { final AdviceApplication execappl = 
	     (AdviceApplication) alistIt.next(); 
          execappl.shadowpoints = execution_sp;
	} // each execution advice
  } // insertBodySP

  private void insertStmtSP(SootMethod method,
                                   List /*<AdviceApplication>*/ advicelist) {
     // set up an empty hash table to store shadow points for pointcuts
     //   attached to statements
	 
     // note use of IdentityHashMap because we want to match by ref
     //   to keystmt
     IdentityHashMap SPhashtable = new IdentityHashMap();
     // iterate through all stmtAdvice ...  
     for (Iterator stmtlist = advicelist.iterator(); stmtlist.hasNext();)
        { final AdviceApplication stmtappl = 
	     (AdviceApplication) stmtlist.next(); 
	   Stmt keystmt = null;
	   if (stmtappl instanceof HandlerAdviceApplication)
             keystmt = ((HandlerAdviceApplication) stmtappl).stmt; 
	   else if (stmtappl instanceof NewStmtAdviceApplication)
             keystmt = ((NewStmtAdviceApplication) stmtappl).stmt;
           else if (stmtappl instanceof StmtAdviceApplication)
             keystmt = ((StmtAdviceApplication) stmtappl).stmt; 
	   else
	     throw new CodeGenException(
	                  "Unknown kind of advice for inside method body: " + 
		           stmtappl);

	   debug("... " + keystmt + " [" + stmtappl + "]");
	   // If stmt is in Hashtable,  use SP entry assciated with it
	   // else, introduce new nops before and after stmt and 
	   // create new SP.
	   if (SPhashtable.containsKey(keystmt))
	     { debug("Already in body ");
	       stmtappl.shadowpoints = 
	        (ShadowPoints) SPhashtable.get(keystmt);
	     }
	   else
	     { debug("Creating a new ShadowPoints");
	       ShadowPoints sp =
	         insertNopsAroundStmt(stmtappl,method,keystmt);
	       // put in the AdviceApplication
	       stmtappl.shadowpoints = sp;
	       // put in the SPhashtable
	       SPhashtable.put(keystmt,sp);
	     }
         } // for each statement
  } // insertStmtSP

  /** Transform body of method so it has exactly one return at the end, 
   * preceeded by nop.  This nop is the end point of shadowpoints.
   * Also insert begin nop either at beginning of body (for methods) or
   * just after <init> call (for constructors). 
   * Return the ShadowPoints object containing new begin and end.
   */
  private ShadowPoints restructureBody(SootMethod method) {
    Body b = method.getActiveBody();
    Chain units = b.getUnits().getNonPatchingChain();
     
    // First look for beginning of shadow point.  If it is a method,
    // a new nop inserted at the beginning of method body.  
    // If it is a constructor, a new nop is inserted
    // right after the call to <init> in the body.    Assume
    // we get code from a Java compiler, and there is only one
    // <init>.  If there is more than one <init> throw an exception.
    Stmt startnop = Jimple.v().newNopStmt();
    if (method.getName().equals("<init>"))
      { debug("Need to insert after call to <init>");
	Unit initstmt = findInitStmt(units);
	units.insertAfter(startnop,initstmt);
      }
    else
      { debug("Need to insert at beginning of method.");  	     
        // now insert a nop for the beginning of real stmts
	// find first statement after identity statements
	Unit firstrealstmt = findFirstRealStmt(units);
	units.insertBefore(startnop,firstrealstmt);
      }

    // Now deal with end point.  Rewire all returns to end of method body.
    // Insert a nop just before the return at end of body.
    Stmt endnop = restructureReturn(method);
    return new ShadowPoints(startnop,endnop);
  } // method restructureBody 
    

  /** Transform body of method so that a nop is placed both before and
   *  after the given stmt.   Branches to the initial stmt must be rewired
   *  to go to the first nop.   Exeception blocks that end at stmt must
   *  be extended to include the second nop.
   */
  private ShadowPoints insertNopsAroundStmt(
      AdviceApplication stmtappl, SootMethod method, Stmt targetstmt) {
    Body b = method.getActiveBody();
    // use a nonPatchingChain because we want to do our own fix of branches
    // and exceptions.
    Chain units = b.getUnits().getNonPatchingChain();  

    // create the beginning and ending nop statements
    Stmt startnop = Jimple.v().newNopStmt();
    Stmt endnop = Jimple.v().newNopStmt();
    Stmt nextstmt = (Stmt) units.getSuccOf(targetstmt);

    // insert new nop stmts into body around targetstmt
   if (stmtappl instanceof HandlerAdviceApplication)
     { // verify that no trap exists that starts at the targetstmt nor 
       // ends at the stmt just after the targetstmt
       for( Iterator trIt = b.getTraps().iterator(); trIt.hasNext(); ) 
         { final Trap tr = (Trap) trIt.next();
           if( tr.getBeginUnit() == targetstmt ||
	       tr.getEndUnit() == nextstmt)
	     throw new CodeGenException(
		 "Not expecting a trap to start at, or " +
		 "end just after a handler stmt");
	  }
       // safe situation, insert nop for start and return null for end
       units.insertAfter(startnop,targetstmt);
       // for the special case of a handler, there is no end to the shadow
       debug("Inserting nop after identity stmt " + targetstmt); 
       return new ShadowPoints(startnop,null);
     }
   else if (stmtappl instanceof NewStmtAdviceApplication)
     { // expecting a new, followed by an <init> (treat these as one unit)
       debug("Inserting nops around pair of stmts " + targetstmt +
	         " ; " + nextstmt);
       units.insertBefore(startnop,targetstmt);
       targetstmt.redirectJumpsToThisTo(startnop);
       units.insertAfter(endnop,nextstmt);
       // if a trap started at the new,
       // it should now start at the startnop
       resetTrapsStart(b,targetstmt,startnop);
       // if a trap ended just after the new, 
       // it should now extend down to the stmt after the endnop 
       resetTrapsEnd(b,nextstmt,(Stmt) units.getSuccOf(endnop));
       return new ShadowPoints(startnop,endnop);
     }

   else if (stmtappl instanceof StmtAdviceApplication)
     { debug("Inserting nops around stmt: " + targetstmt);
       units.insertBefore(startnop,targetstmt);
       targetstmt.redirectJumpsToThisTo(startnop);
       units.insertAfter(endnop,targetstmt);
       // if a trap started at targetstmt 
       // it should now start at the startnop
       resetTrapsStart(b,targetstmt,startnop);
       // if a trap ended just after the targetstmt, 
       // it should now extend down to the stmt after the endnop 
       resetTrapsEnd(b,nextstmt,(Stmt) units.getSuccOf(endnop));
       return new ShadowPoints(startnop,endnop);
     }

   else
     throw new CodeGenException(
                 "Unknown kind of advice for inside method body: " + 
                 stmtappl);
  } // method insertNopAroundStmt

    /* --------------------------- PASS 2 --------------------------*/

    public void setShadowPointsPass2(SootClass sc) {
      debug("--- BEGIN Setting ShadowPoints Pass2 for class " +
	  sc.getName());

      // for each method in the class, look to see if it is an <init> and
      //    needs to be inlined
      debug("Iterating through methods, looking for <init> methods ");
      debug("that need inlining"); 

      LinkedList methodlist = new LinkedList();

      for( Iterator methodIt = sc.getMethods().iterator(); 
	   methodIt.hasNext(); ) {

	 // get the next method
         final SootMethod method = (SootMethod) methodIt.next();

	 // nothing to do for abstract or native methods 
         if( method.isAbstract() ) continue;
         if( method.isNative() ) continue;
         // FIXME: shouldn't have to check name
	 if( !method.getName().equals("<init>") ) continue;


	 // get all the advice list for this method
         MethodAdviceList adviceList = 
	     GlobalAspectInfo.v().getAdviceList(method);

	 // if it has init or preinit advice list, inline body 
	 if ((adviceList != null) && 
	     (adviceList.hasPreinitializationAdvice() ||
	      adviceList.hasInitializationAdvice()
	     )
	    )
           { debug("Must inline body of " + method.getName());
	     // add to list of methods to process
	     // if not restructured, do it now 
	     if (!adviceList.isRestructured())
	       { restructureReturn(method);
		 adviceList.restructuringDone();
	       }

	     // TODO: put call to inliner 
	     methodlist.add(method);
	   }
	}

      // now go back and put in the init and preinit ShadowPoints
      for (Iterator methodIt = methodlist.iterator(); methodIt.hasNext();)
	{ // ---- process next <init> method
	  final SootMethod method = (SootMethod) methodIt.next();
          MethodAdviceList adviceList = 
	     GlobalAspectInfo.v().getAdviceList(method);
          debug("   --- BEGIN Setting ShadowPoints Pass2 for method " + 
	                method.getName());
	  debug("Advice for method " + method.getName() + " is : \n" +
	               adviceList);

	 // --- First look at preinitialization pointcuts 
	 if (adviceList.hasPreinitializationAdvice())
	   insertPreinitializationSP(method,adviceList.preinitializationAdvice);

	 // --- Then look at initialization pointcuts 
	 if (adviceList.hasInitializationAdvice())
	    insertInitializationSP(method,adviceList.initializationAdvice);

	 debug("   --- END Setting ShadowPoints Pass2 for method " + 
	                    method.getName() + "\n");
        } // for each method

      debug(" --- END Setting ShadowPoints Pass2 for class " + sc.getName() + "\n");
    } // setShadowPointsPass2


  private void insertInitializationSP(SootMethod method,
                                   List /*<AdviceApplication>*/ advicelist) {
     // should only be for methods called <init>
     ShadowPoints initialization_sp = null;
     debug("Initialization for <init> in method: " + method.getName()); 
     // check that name is <init>, otherwise throw exception
     if (method.getName().equals("<init>"))
        { debug("dealing with init of <init>");
	  Body b = method.getActiveBody();
	  Chain units = b.getUnits().getNonPatchingChain();
	  Stmt startnop = Jimple.v().newNopStmt();
	  Stmt endnop = Jimple.v().newNopStmt();
          // insert startnop just after call to <init>,  
	  Stmt initstmt = findInitStmt(units);
	  units.insertAfter(startnop,initstmt);
	  // insert endnop just after just before final ret
	  units.insertBefore(endnop,units.getLast());
	  initialization_sp = new ShadowPoints(startnop,endnop); 
          // make all initializatin AdviceApplications refer to the
          // shadowpoints object just constructed.
          for (Iterator alistIt = advicelist.iterator(); alistIt.hasNext();)
             { final AdviceApplication initappl = 
	               (AdviceApplication) alistIt.next(); 
                initappl.shadowpoints = initialization_sp;
	     } // each initialization advice
	}
     else
       throw new CodeGenException("Constructor advice on non <init>"); 
  } // insertInitializationSP


  private void insertPreinitializationSP(SootMethod method,
                                   List /*<AdviceApplication>*/ advicelist) {
     // should only be formethods called <init>
     ShadowPoints preinitialization_sp = null;
     debug("Preinitialization for <init> in method: " + method.getName()); 
     // check that name is <init>, otherwise throw exception
     if (method.getName().equals("<init>"))
        { Body b = method.getActiveBody();
          Chain units = b.getUnits().getNonPatchingChain();
	  Stmt startnop = Jimple.v().newNopStmt();
	  Stmt endnop = Jimple.v().newNopStmt();
	  // insert startnop at beginning of method, just before first
	  // real statement
	  Stmt firstreal = findFirstRealStmt(units); 
	  units.insertBefore(startnop,firstreal);
          // insert endnop just before call to <init>,  
	  Stmt initstmt = findInitStmt(units);
	  units.insertBefore(endnop,initstmt);
	  preinitialization_sp = new ShadowPoints(startnop,endnop); 
          // make all preinitialization AdviceApplications refer to the
          // shadowpoints object just constructed.
          for (Iterator alistIt = advicelist.iterator(); alistIt.hasNext();)
             { final AdviceApplication preinitappl = 
	               (AdviceApplication) alistIt.next(); 
                preinitappl.shadowpoints = preinitialization_sp;
	     } // each initialization advice
	}
     else
       throw new CodeGenException("Constructor advice on non <init>"); 
  } // insertPreinitializationSP

  /* ------------------- Utility restructurers ----------------------- */

  /** Given a Chain for a body of an <init> method, find the call to
   *  the <init> corresponding to either a this() or super() call.  Check
   *  that there is exactly one such <init>, otherwise throw a 
   *  CodeGenException.   Return the <init> Stmt.
   */
  public Stmt findInitStmt(Chain units)
    { // look for the <init> 
      Iterator it = units.snapshotIterator();
      Stmt initstmt = null;

      // get the "this", should be first identity statement
      Stmt first = (Stmt) it.next();
      Local thisloc = null;
      if (first instanceof IdentityStmt)
        thisloc = (Local) 
	        ((IdentityStmt) first).getLeftOp();//the local for "this" 
	else
	  throw new CodeGenException("Expecting an identity stmt for this");
	
      int countinits = 0;
      while ( it.hasNext() )
        { Stmt u = (Stmt) it.next();
          debug("Looking at stmt " + u);
          if ((u instanceof InvokeStmt) && 
             ((InvokeStmt) u).getInvokeExpr() instanceof SpecialInvokeExpr &&
	     ((SpecialInvokeExpr) ((InvokeStmt) u).getInvokeExpr()).
		                                  getBase().equals(thisloc) )
	    { debug("Found <init> " + u);
	      countinits++;
	      if (countinits == 1) // great, found it
	        initstmt = u;  
	      else
	        throw new CodeGenException("Expecting only one <init>");
             }	 
          } // all units
	 
       if (countinits == 0)     
         throw new CodeGenException("Could not find a matching <init>");

       return(initstmt);
     }

   /** Given a Chain for the body of a method,  find the first "real"
    *  stmt (i.e. not an identity stmt) and return a reference to it.
    */
   public Stmt findFirstRealStmt(Chain units)
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
   public void resetTrapsEnd(Body b, Stmt oldend, Stmt newend)
     { for( Iterator trIt = b.getTraps().iterator(); trIt.hasNext(); ) 
         { final Trap tr = (Trap) trIt.next();
           if( tr.getEndUnit() == oldend ) 
	     tr.setEndUnit(newend);  
	  }
      }

   /** update all traps that used to start at oldstart to now start at newstart
    */
   public void resetTrapsStart(Body b, Stmt oldstart, Stmt newstart)
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
  public Stmt restructureReturn(SootMethod method) {
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

} // class ShadowPointsSetter
