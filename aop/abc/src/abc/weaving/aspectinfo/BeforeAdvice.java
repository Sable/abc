package abc.weaving.aspectinfo;

import java.util.Iterator;

import polyglot.util.Position;

import soot.*;
import soot.jimple.*;
import soot.util.Chain;

import abc.weaving.matching.*;
import abc.weaving.residues.Residue;
import abc.weaving.residues.AlwaysMatch;
import abc.weaving.weaver.*;
import abc.soot.util.LocalGeneratorEx;

/** Advice specification for before advice. */
public class BeforeAdvice extends AbstractAdviceSpec {
    public BeforeAdvice(Position pos) {
	super(pos);
    }

    public String toString() {
	return "before";
    }

    public Residue matchesAt(WeavingEnv we,ShadowMatch sm) {
	return sm.supportsBefore() ? AlwaysMatch.v : null;
    }

    private static void debug(String message) { 
	if(abc.main.Debug.v().beforeWeaver) 
	    System.err.println("BEF*** " + message);
    }

    public void weave(SootMethod method,LocalGeneratorEx localgen,AdviceApplication adviceappl) {
	WeavingContext wc=adviceappl.advice.makeWeavingContext();
	doWeave(method,localgen,adviceappl,adviceappl.getResidue(),wc);
    }

    static void doWeave(SootMethod method,
			LocalGeneratorEx localgen,
			AdviceApplication adviceappl,
			Residue residue,
			WeavingContext wc) {

	ShadowPoints shadowpoints=adviceappl.shadowmatch.sp;
	AbstractAdviceDecl advicedecl=adviceappl.advice;

	debug("Before weaver running at "+shadowpoints.getShadowMatch());
	
	Body b = method.getActiveBody();
        // this non patching chain is needed so that Soot doesn't "Fix" 
        // the traps. 
        Chain units = b.getUnits().getNonPatchingChain();

	// find location to weave in statements, 
	// just after beginning of join point shadow
	Stmt beginshadow = shadowpoints.getBegin();
	Stmt followingstmt = (Stmt) units.getSuccOf(beginshadow);

	Stmt failpoint = Jimple.v().newNopStmt();
	units.insertBefore(failpoint,followingstmt);

	debug("Weaving in residue: "+residue);

	// weave in residue
	Stmt endresidue=residue.codeGen
	    (method,localgen,units,beginshadow,failpoint,true,wc);

	debug("Weaving in advice execution statements");

        Chain stmts = advicedecl.makeAdviceExecutionStmts(adviceappl,localgen,wc);

        debug("Generated stmts: " + stmts);
	
	for( Iterator nextstmtIt = stmts.iterator(); nextstmtIt.hasNext(); ) {
	
	    final Stmt nextstmt = (Stmt) nextstmtIt.next();
	    units.insertBefore(nextstmt,failpoint);
	}
    }
}
