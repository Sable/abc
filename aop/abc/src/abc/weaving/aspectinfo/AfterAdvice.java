package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

import abc.weaving.matching.*;
import abc.weaving.residues.Residue;
import abc.weaving.residues.AlwaysMatch;
import abc.weaving.weaver.WeavingContext;
import abc.soot.util.LocalGeneratorEx;

/** Advice specification for after advice. */
public class AfterAdvice extends AbstractAdviceSpec {
    private AfterReturningAdvice returning;
    private AfterThrowingAdvice throwing;

    public AfterAdvice(Position pos) {
	super(pos);
	returning=new AfterReturningAdvice(pos);
	throwing=new AfterThrowingAdvice(pos);
    }

    public String toString() {
	return "after";
    }

    public Residue matchesAt(WeavingEnv we,ShadowMatch sm) {
	return sm.supportsAfter() ? AlwaysMatch.v : null;
    }

    public void weave(SootMethod method,LocalGeneratorEx localgen,AdviceApplication adviceappl) {
	// We want separate contexts because we generate the residue twice. 
	// Do throwing weave first, so that the exception ranges work out correctly.
	throwing.weave(method,localgen,adviceappl);
	returning.weave(method,localgen,adviceappl);
    }

    void doWeave(SootMethod method,LocalGeneratorEx localgen,
		 AdviceApplication adviceappl,Residue residue,
		 WeavingContext wc) {
	throwing.doWeave(method,localgen,adviceappl,residue,wc);
	returning.doWeave(method,localgen,adviceappl,residue,wc);
    }

}
