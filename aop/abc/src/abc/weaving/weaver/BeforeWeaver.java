package abc.weaving.weaver;

import soot.*;
import soot.util.*;
import soot.jimple.*;
import java.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;
import soot.javaToJimple.LocalGenerator;

/** Handle before weavering.
 * @author Laurie Hendren
 * @author Jennifer Lhotak
 * @author Ondrej Lhotak
 * @date May 6, 2004
 */

public class BeforeWeaver {

   /** set to false to disable debugging messages for Before Weaver */
   public static boolean debug = true;

   private static void debug(String message)
     { if (debug) System.err.println("BEF*** " + message);
     }


    public static void doWeave(SootMethod method, LocalGenerator localgen,
	                      AdviceApplication adviceappl)
      { debug("Handling before: " + adviceappl);
        Body b = method.getActiveBody();
        // this non patching chain is needed so that Soot doesn't "Fix" 
        // the traps. 
        Chain units = b.getUnits().getNonPatchingChain();
	AdviceDecl advicedecl = adviceappl.advice;
	SootClass aspect = advicedecl.getAspect().
	                          getInstanceClass().getSootClass();

	// <AspectType> aspectref;
        Local aspectref = localgen.generateLocal( aspect.getType() );
	debug("Generated new local: " + aspectref);

	// smt1:  aspectref = <AspectType>.aspectOf();
        AssignStmt stmt1 =  
	  Jimple.v().newAssignStmt( 
	      aspectref, 
	      Jimple.v().newStaticInvokeExpr(
		aspect.getMethod("aspectOf", new ArrayList())));
	debug("Generated stmt1: " + stmt1);

	// stmt2:  <aspectref>.<advicemethod>();
        InvokeStmt stmt2 = PointcutCodeGen.makeAdviceInvokeStmt
	                                 (aspectref,adviceappl,units);
        debug("Generated stmt2: " + stmt2);

	// weave in statements just after beginning of join point shadow
	Stmt beginshadow = adviceappl.shadowpoints.getBegin();
	units.insertAfter(stmt2,beginshadow);
	units.insertAfter(stmt1,beginshadow);
      } // method doWeave 


}
