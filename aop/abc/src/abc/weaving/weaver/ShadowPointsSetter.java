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
         debug("   --- BEGIN Setting ShadowPoints Pass1 for method " + 
	                method.getName());

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

	 // ---- we have some advice, so set things up for this method
	 debug("Advice for method " + method.getName() + " is : \n" +
	               adviceList);

	 // --- First deal with execution pointcuts
	 if (adviceList.hasBodyAdvice())
	    insertBodySP(method,adviceList.bodyAdvice);

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
                            List /*<AdviceApplication>*/ advicelist) {

    debug("Need to transform for execution in method: " + method.getName()); 

    // restructure returns, and insert begin and end nops
    ShadowPoints execution_sp = restructureBody(method); 
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
	 
     Hashtable SPhashtable = new Hashtable();
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
	     // TODO: put call to inliner 
	     // add to list of methods to process
	     methodlist.add(method);
	   }
	}

      // now go back and put in the init and preinit ShadowPoints
      for (Iterator methodIt = methodlist.iterator(); methodIt.hasNext();)
	{ // ---- process next <init> method
	  final SootMethod method = (SootMethod) methodIt.next();
          MethodAdviceList adviceList = 
	     GlobalAspectInfo.v().getAdviceList(method);
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
       // insert nop at beginning of method and just before call 
       // to <init>,  for preintialization join points
	     
       // make all preinitializatin AdviceApplications refer to the
       // shadowpoints object just constructed.
       debug("dealing with init of <init>");
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
       // insert nop at beginning of method and just before call 
       // to <init>,  for preintialization join points
	     
       // make all preinitializatin AdviceApplications refer to the
       // shadowpoints object just constructed.
       debug("dealing with preinit of <init>");
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

    // update any traps to end at nop
    for( Iterator trIt = b.getTraps().iterator(); trIt.hasNext(); ) 
      { final Trap tr = (Trap) trIt.next();
        if( tr.getEndUnit() == units.getLast() ) 
	  tr.setEndUnit(endnop);  
       }

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
