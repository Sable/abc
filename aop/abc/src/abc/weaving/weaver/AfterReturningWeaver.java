package abc.weaving.weaver;

import java.util.ArrayList;
import java.util.Iterator;

import soot.Body;
import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.AssignStmt;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.util.Chain;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.aspectinfo.AdviceSpec;
import abc.weaving.matching.AdviceApplication;

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


    public static void doWeave(SootMethod method, LocalGeneratorEx localgen,
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
