package abc.weaving.aspectinfo;

import java.util.*;

import polyglot.util.Position;

import soot.*;
import soot.jimple.*;
import soot.util.Chain;

import abc.weaving.matching.*;
import abc.weaving.residues.Residue;
import abc.weaving.residues.AlwaysMatch;
import abc.weaving.weaver.ShadowPoints;
import abc.weaving.weaver.WeavingContext;
import abc.soot.util.LocalGeneratorEx;

/** Advice specification for after returning advice without return variable binding. */
public class AfterReturningAdvice extends AbstractAfterAdvice {
    public AfterReturningAdvice(Position pos) {
	super(pos);
    }

    public String toString() {
	return "after returning";
    }

    private static void debug(String message) { 
	if(abc.main.Debug.v().afterReturningWeaver) 
	    System.err.println("AFR*** " + message);
    }

    public void weave(SootMethod method,
			     LocalGeneratorEx localgen,
			     AdviceApplication adviceappl) {
	WeavingContext wc=adviceappl.advice.makeWeavingContext();
	doWeave(method,localgen,adviceappl,adviceappl.getResidue(),wc);
    }
    

    void doWeave(SootMethod method, LocalGeneratorEx localgen,
		 AdviceApplication adviceappl,Residue residue,
		 WeavingContext wc) {
	
	ShadowPoints shadowpoints=adviceappl.shadowmatch.sp;
	AbstractAdviceDecl advicedecl=adviceappl.advice;

	debug("In after returning weaver");
        Body b = method.getActiveBody();
	// Use the non-patching chain to stop soot "fixing" up the jumps
        Chain units = b.getUnits().getNonPatchingChain();

	Stmt endshadow = shadowpoints.getEnd();
	Stmt prevstmt = (Stmt) units.getPredOf(endshadow);

	Stmt failpoint = Jimple.v().newNopStmt();
	units.insertBefore(failpoint,endshadow);

	debug("generating residue code");
	Stmt endresidue=residue.codeGen
	    (method,localgen,units,prevstmt,failpoint,true,wc);

	debug("making advice execution statements");
        Chain stmts = advicedecl.makeAdviceExecutionStmts(adviceappl,localgen,wc);
        debug("Generated stmts: " + stmts);

	// weave in statements just before end of join point shadow

	for (Iterator stmtlist = stmts.iterator(); stmtlist.hasNext(); )
	  { Stmt nextstmt = (Stmt) stmtlist.next();
	    units.insertBefore(nextstmt,failpoint);
	  }
	debug("after returning weaver finished");
      } // method doWeave 

}
