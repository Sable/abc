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

    private static void spsdebug(String message)
      { if (debug) System.err.println("SPS*** " + message);
      }	

    /** Set all ShadowPoints for AdviceApplications in class sc. */
    public void setShadowPoints(SootClass sc) {
      spsdebug("--- BEGIN Setting ShadowPoints for class " + sc.getName());

      // for each method in the class 
      for( Iterator methodIt = sc.getMethods().iterator(); 
	   methodIt.hasNext(); ) {

	 // get the next method
         final SootMethod method = (SootMethod) methodIt.next();
         spsdebug("   --- BEGIN Setting ShadowPoints for method " + 
	                method.getName());

	 // nothing to do for abstract or native methods 
         if( method.isAbstract() ) continue;
         if( method.isNative() ) continue;

	 // get all the advice list for this method
         MethodAdviceList adviceList = 
	     GlobalAspectInfo.v().getAdviceList(method);

	 // if no advice list for this method, nothing to do
	 if ((adviceList == null) || adviceList.isEmpty())
           { spsdebug("No advice list for method " + method.getName());
	     continue;
	   }

	 // ---- we have some advice, so set things up for this method
	 spsdebug("Advice for method " + method.getName() + " is : \n" +
	               adviceList);

	 // --- First deal with execution pointcuts
	 if (adviceList.hasBodyAdvice())
	    insertBodySP(method,adviceList.bodyAdvice);

	 // --- Then look at constructor pointcuts 
	 if (adviceList.hasConstructorAdvice())
	    insertConstructorSP(method,adviceList.constructorAdvice);

	 // ---- Then deal with stmt pointcuts that are in the body
         if (adviceList.hasStmtAdvice())
	    insertStmtSP(method,adviceList.stmtAdvice);

	 spsdebug("   --- END Setting ShadowPoints for method " + 
	                    method.getName() + "\n");
       } // for each method

      spsdebug(" --- END Setting ShadowPoints for class " + sc.getName() + "\n");
    } // setShadowPoints

  private void insertBodySP(SootMethod method, 
                            List /*<AdviceApplication>*/ advicelist) {

    spsdebug("Need to transform for execution in method: " + method.getName()); 

    // restructure returns, and insert begin and end nops
    ShadowPoints execution_sp = restructureBody(method); 
    spsdebug("Restructured method looks like: \n" + method);
    spsdebug("ShadowPoints are: " + execution_sp);
	     
    // make all execution AdviceApplications refer to the shadowpoints
    // object just constructed.
    
  } // insertBodySP

  private void insertConstructorSP(SootMethod method,
                                   List /*<AdviceApplication>*/ advicelist) {
     // should only be for //     methods called <init>
     // TODO:  must make another weaving pass for initialization 
     ShadowPoints preinitialization_sp = null;
     spsdebug("Need to transform for preinit in method: " + method.getName()); 
     // check that name is <init>, otherwise throw exception
     if (method.getName().equals("<init>"))
       // insert nop at beginning of method and just before call 
       // to <init>,  for preintialization join points
	     
       // make all preinitializatin AdviceApplications refer to the
       // shadowpoints object just constructed.
       spsdebug("dealing with preinit of <init>");
     else
       // TODO: put this back when matcher is fixed.
       // throw new CodeGenException("Constructor advice on non <init>"); 
       spsdebug("Ignoring preinit advice on method " + method.getName());
  } // insertConstructorSP

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

	   spsdebug("... " + keystmt + " [" + stmtappl + "]");

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
    LocalGenerator localgen = new LocalGenerator(b);
    Chain units = b.getUnits().getNonPatchingChain();
     
    // First look for beginning of shadow point.  If it is a method,
    // a new nop inserted at the beginning of method body.  
    // If it is a constructor, a new nop is inserted
    // right after the call to <init> in the body.    Assume
    // we get code from a Java compiler, and there is only one
    // <init>.  If there is more than one <init> throw an exception.
    Stmt startnop = Jimple.v().newNopStmt();
    if (method.getName().equals("<init>"))
      { spsdebug("Need to insert after call to <init>");
	// look for the <init> 
        Iterator it = units.snapshotIterator();
	// get the "this", should be first identity statement
	Stmt first = (Stmt) it.next();
	Local thisloc = null;
        if (first instanceof IdentityStmt)
	  thisloc = (Local) 
	              ((IdentityStmt) first).getLeftOp();//the local for "this" 
	else
	  throw new CodeGenException("Expecting an identify stmt for this");
	
	int countinits = 0;
        while ( it.hasNext() )
          { Stmt u = (Stmt) it.next();
	    spsdebug("Looking at stmt " + u);
	    if ((u instanceof InvokeStmt) && 
		((InvokeStmt) u).getInvokeExpr() instanceof SpecialInvokeExpr &&
		((SpecialInvokeExpr) ((InvokeStmt) u).getInvokeExpr()).
		                                  getBase().equals(thisloc) )
	       { spsdebug("Found <init> " + u);
		 countinits++;
		 if (countinits == 1) // great, found it
		   // TODO:  have to fix this
		   units.insertAfter(startnop,u);
		 else
		   throw new CodeGenException("Expecting only one <init>");
               }	 
          } // all units
	 
        if (countinits == 0)     
	  throw new CodeGenException("Could not find a matching <init>");
      }
    else
      { spsdebug("Need to insert at beginning of method.");  	     
        // now insert a nop for the beginning of real stmts
	// find first statement after identity statements
	Iterator it = units.snapshotIterator();
	while ( it.hasNext() )
          { Stmt u = (Stmt) it.next();
	    if (! (u instanceof IdentityStmt)) // first non-IdentityStmt
	      { units.insertBefore(startnop,u);
		break;
              }
           }
      }

    // Now deal with end point.  Rewire all returns to end of method body.
    // Insert a nop just before the return at end of body.
    Stmt endnop = Jimple.v().newNopStmt();
    Stmt last = (Stmt) units.getLast();
    units = b.getUnits();  // want a patching chain here, to make sure
                           // gotos are rewired to go to the inserted nop

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


    return new ShadowPoints(startnop,endnop);
  } // method restructureBody 

} // class ShadowPointsSetter
