package abc.weaving.weaver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

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
import abc.weaving.matching.AdviceApplication;

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


    public static void doWeave(SootMethod method, LocalGeneratorEx localgen,
			       AdviceApplication adviceappl)
      { debug("Handling before: " + adviceappl);
        Body b = method.getActiveBody();
        // this non patching chain is needed so that Soot doesn't "Fix" 
        // the traps. 
        Chain units = b.getUnits().getNonPatchingChain();

	WeavingContext wc=PointcutCodeGen.makeWeavingContext(adviceappl);

	// find location to weave in statements, 
	// just after beginning of join point shadow
	Stmt beginshadow = adviceappl.shadowpoints.getBegin();
	Stmt followingstmt = (Stmt) units.getSuccOf(beginshadow);

	Stmt failpoint = Jimple.v().newNopStmt();
	units.insertBefore(failpoint,followingstmt);

	// weave in residue
	Stmt endresidue=adviceappl.residue.codeGen
	    (method,localgen,units,beginshadow,failpoint,wc);

	AbstractAdviceDecl advicedecl = adviceappl.advice;

        Chain stmts = advicedecl.makeAdviceExecutionStmts
	    (adviceappl,localgen,wc);
        debug("Generated stmts: " + stmts);
	
	for (Iterator stmtlist = stmts.iterator(); stmtlist.hasNext(); )
	  { Stmt nextstmt = (Stmt) stmtlist.next();
	  units.insertBefore(nextstmt,failpoint);
	  }

      } // method doWeave 

}
