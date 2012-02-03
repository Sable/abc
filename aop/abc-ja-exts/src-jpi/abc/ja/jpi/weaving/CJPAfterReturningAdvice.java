package abc.ja.jpi.weaving;

import java.util.Iterator;

import polyglot.util.Position;
import soot.Body;
import soot.SootMethod;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.util.Chain;
import abc.ja.jpi.jrag.JPITypeDecl;
import abc.ja.jpi.jrag.TypeAccess;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.residues.Residue;
import abc.weaving.tagkit.InstructionShadowTag;
import abc.weaving.tagkit.InstructionSourceTag;
import abc.weaving.weaver.AdviceInliner;
import abc.weaving.weaver.ShadowPoints;
import abc.weaving.weaver.WeavingContext;

/** Advice specification for after returning advice without return variable binding. 
 *  @author Aske Simon Christensen
 *  @author Ganesh Sittampalam
 */
public class CJPAfterReturningAdvice extends CJPAbstractAfterAdvice {
    public CJPAfterReturningAdvice(Position pos, JPITypeDecl jpiTypeDecl) {
    	super(pos,jpiTypeDecl);
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
    AdviceInliner.v().addShadowMethod(method);
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
    debug("src/shadow: " + adviceappl.shadowmatch.shadowId + " " + adviceappl.advice.sourceId);
    // store shadow/source tag for this residue in weaving context
    wc.setShadowTag(new InstructionShadowTag(adviceappl.shadowmatch.shadowId));
    wc.setSourceTag(new InstructionSourceTag(adviceappl.advice.sourceId));
	residue.codeGen
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
