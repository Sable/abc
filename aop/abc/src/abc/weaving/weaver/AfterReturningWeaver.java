package abc.weaving.weaver;

import soot.*;
import soot.util.*;
import soot.jimple.*;
import java.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;
import soot.javaToJimple.LocalGenerator;

/** Handle after returning weavering.
 * @author Laurie Hendren
 * @author Jennifer Lhotak
 * @author Ondrej Lhotak
 * @date May 6, 2004
 */

public class AfterReturningWeaver {

   /** set to false to disable debugging messages for After Returning Weaver */
   public static boolean debug = true;

   private static void afrdebug(String message)
     { if (debug) System.err.println("AFR*** " + message);
     }


    public static void doWeave(SootMethod method, LocalGenerator localgen,
	                      AdviceApplication adviceappl)
      { afrdebug("Handling after returning: " + adviceappl);
        Body b = method.getActiveBody();
        Chain units = b.getUnits();
	AdviceDecl advicedecl = adviceappl.advice;
	AdviceSpec advicespec = advicedecl.getAdviceSpec();
	SootClass aspect = advicedecl.getAspect().
	                          getInstanceClass().getSootClass();
	SootMethod advicemethod = advicedecl.getImpl().getSootMethod();

	// <AspectType> aspectref;
        Local aspectref = localgen.generateLocal( aspect.getType() );
	afrdebug("Generated new local: " + aspectref);

	// smt1:  aspectref = <AspectType>.aspectOf();
        AssignStmt stmt1 =  
	  Jimple.v().newAssignStmt( 
	      aspectref, 
	      Jimple.v().newStaticInvokeExpr(
		aspect.getMethod("aspectOf", new ArrayList())));
	afrdebug("Generated stmt1: " + stmt1);

	// stmt2:  <aspectref>.<advicemethod>();
        InvokeStmt stmt2 =
          Jimple.v().newInvokeStmt( 
	    Jimple.v().newVirtualInvokeExpr( aspectref, advicemethod ) );
        afrdebug("Generated stmt2: " + stmt2);

	// weave in statements just before end of join point shadow
	Stmt endshadow = adviceappl.shadowpoints.getEnd();
	units.insertBefore(stmt1,endshadow);
	units.insertBefore(stmt2,endshadow);
      } // method doWeave 
    
}
