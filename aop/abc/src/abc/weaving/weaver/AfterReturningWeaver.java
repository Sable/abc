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
import abc.soot.util.*;
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
	// Use the non-patching chain to stop soot "fixing" up the jumps
        Chain units = b.getUnits().getNonPatchingChain();

	WeavingContext wc=PointcutCodeGen.makeWeavingContext(adviceappl);
	
	Stmt endshadow = adviceappl.shadowpoints.getEnd();
	Stmt prevstmt = (Stmt) units.getPredOf(endshadow);

	Stmt failpoint = Jimple.v().newNopStmt();
	units.insertBefore(failpoint,endshadow);

	Stmt endresidue=adviceappl.residue.codeGen
	    (method,localgen,units,prevstmt,failpoint,wc);

	AdviceDecl advicedecl = adviceappl.advice;
	AdviceSpec advicespec = advicedecl.getAdviceSpec();
	SootClass aspect = advicedecl.getAspect().
	                          getInstanceClass().getSootClass();
	SootMethod advicemethod = advicedecl.getImpl().getSootMethod();


	// <AspectType> aspectref;
        Local aspectref = localgen.generateLocal( aspect.getType(), "theAspect" );
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
                            (aspectref,adviceappl,units,localgen,wc);
        debug("Generated stmts2: " + stmts2);

	// weave in statements just before end of join point shadow

	units.insertAfter(stmt1,endresidue);
	for (Iterator stmtlist = stmts2.iterator(); stmtlist.hasNext(); )
	  { Stmt nextstmt = (Stmt) stmtlist.next();
	    units.insertBefore(nextstmt,failpoint);
	  }
      } // method doWeave 

}
