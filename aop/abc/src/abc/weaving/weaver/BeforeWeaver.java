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

   private static void debug(String message)
     { if (abc.main.Debug.v().beforeWeaver) 
         System.err.println("BEF*** " + message);
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
        Chain stmts2 = PointcutCodeGen.makeAdviceInvokeStmt
	                       (aspectref,adviceappl,units,localgen);
        debug("Generated stmts2: " + stmts2);

	// weave in statements just after beginning of join point shadow
	Stmt beginshadow = adviceappl.shadowpoints.getBegin();
	Stmt followingstmt = (Stmt) units.getSuccOf(beginshadow);
	units.insertAfter(stmt1,beginshadow);
	for (Iterator stmtlist = stmts2.iterator(); stmtlist.hasNext(); )
	  { Stmt nextstmt = (Stmt) stmtlist.next();
	    units.insertBefore(nextstmt,followingstmt);
	  }
      } // method doWeave 

}
