package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.Residue;
import abc.weaving.residues.AlwaysMatch;
import abc.weaving.weaver.WeavingContext;

/** Advice specification for after throwing advice with exception variable binding.
 *  @author Aske Simon Christensen
 *  @author Ganesh Sittampalam
 */
public class AfterThrowingArgAdvice extends AfterThrowingAdvice {
    private Formal formal;

    public AfterThrowingArgAdvice(Formal formal, Position pos) {
	super(pos);
	this.formal = formal;
    }

    public Formal getFormal() {
	return formal;
    }

    public String toString() {
	return "after throwing arg";
    }

    // We inherit the matchesAt method from AfterThrowingAdvice,
    // because the binding of the formal is best done as a special
    // case in the weaver for after throwing advice

    public RefType getCatchType() {
	return (RefType) (formal.getType().getSootType());
    }

    public void bindException(WeavingContext wc,AbstractAdviceDecl ad,Local exception) {
	wc.arglist.setElementAt(exception,((AdviceDecl) ad).getFormalIndex(formal.getName()));
    }
}
