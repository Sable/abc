package abc.weaving.matching;

import soot.SootMethod;
import abc.weaving.residues.ContextValue;

/** A "body" shadow match
 *  @author Ganesh Sittampalam
 */

public abstract class BodyShadowMatch extends ShadowMatch {
    protected BodyShadowMatch(SootMethod container) {
	super(container);
    }

    public ShadowMatch getEnclosing() {
	return this;
    }

    public ContextValue getTargetContextValue() {
	return getThisContextValue();
    }
}
