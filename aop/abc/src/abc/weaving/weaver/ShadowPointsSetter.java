package abc.weaving.weaver;

import soot.*;
import soot.util.*;
import soot.jimple.*;
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
 * @date May 3, 2004
 */

public class ShadowPointsSetter {

    /** set to false to disable debugging messages for ShadowPointsSetter */
    public static boolean debug = true;

    private static void spsdebug(String message)
      { if (debug) System.err.println("SPS***" + message);
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
    ShadowPoints execution_sp = null;
    // transform body of method so it 
    // has exactly one return at the end, preceeded by nop.  This
    // nop is the end point of shadowpoints.
    spsdebug("Need to transform for execution in method: " + method.getName()); 
	     
    // now look for beginning of shadow point.  If it is a method,
    // beginning is a new nop inserted at the beginning of method
    // body.    If it is a constructor, it is a new nop inserted
    // right after the call to <init> in the body.    Assuming 
    // we get code from a Java compiler, and there is only one
    // <init>.
    if (method.getName().equals("<init>"))
      spsdebug("Need to insert after call to <init>");
    else
      spsdebug("Need to insert at beginning of method.");  	     
	     
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

	    spsdebug("dealing with stmt: " + keystmt);

	    // If stmt is in Hashtable,  use SP entry assciated with it
	    // else, introduce new nops before and after stmt and 
	    // create new SP.
	      
	  } // for each statement
  } // insertStmtSP

} // class ShadowPointsSetter
