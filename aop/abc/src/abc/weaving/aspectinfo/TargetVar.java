package abc.weaving.aspectinfo;

import polyglot.util.Position;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.*;

/** Handler for <code>target</code> condition pointcut with a variable argument. */
public class TargetVar extends TargetAny {
    private Var var;

    public TargetVar(Var var,Position pos) {
	super(pos);
	this.var = var;
    }

    /** Get the pointcut variable that is bound by this
     *  <code>target</code> pointcut.
     */
    public Var getVar() {
	return var;
    }

    public String toString() {
	return "target("+var+")";
    }

    protected Residue matchesAt(WeavingEnv we,ContextValue cv) {
	return Bind.construct
	    (cv,we.getAbcType(var).getSootType(),we.getWeavingVar(var));
    }

}
