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
import abc.weaving.aspectinfo.AbstractAdviceDecl;
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

	AbstractAdviceDecl advicedecl = adviceappl.advice;

        Chain stmts = advicedecl.makeAdviceExecutionStmts
                            (adviceappl,localgen,wc);
        debug("Generated stmts: " + stmts);

	// weave in statements just before end of join point shadow

	for (Iterator stmtlist = stmts.iterator(); stmtlist.hasNext(); )
	  { Stmt nextstmt = (Stmt) stmtlist.next();
	    units.insertBefore(nextstmt,failpoint);
	  }
      } // method doWeave 

}
