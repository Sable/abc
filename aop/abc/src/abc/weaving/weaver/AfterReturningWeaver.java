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


   private static void debug(String message)
     { if (abc.main.Debug.v().afterReturningWeaver) 
          System.err.println("AFR*** " + message);
     }


    public static void doWeave(SootMethod method, LocalGenerator localgen,
	                      AdviceApplication adviceappl)
      { debug("Handling after returning: " + adviceappl);
        Body b = method.getActiveBody();
        Chain units = b.getUnits();
	AdviceDecl advicedecl = adviceappl.advice;
	AdviceSpec advicespec = advicedecl.getAdviceSpec();
	SootClass aspect = advicedecl.getAspect().
	                          getInstanceClass().getSootClass();
	SootMethod advicemethod = advicedecl.getImpl().getSootMethod();

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

	// weave in statements just before end of join point shadow
	Stmt endshadow = adviceappl.shadowpoints.getEnd();
	units.insertBefore(stmt1,endshadow);
	for (Iterator stmtlist = stmts2.iterator(); stmtlist.hasNext(); )
	  { Stmt nextstmt = (Stmt) stmtlist.next();
	    units.insertBefore(nextstmt,endshadow);
	  }
      } // method doWeave 

}
