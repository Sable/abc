package abc.weaving.aspectinfo;

import polyglot.util.Position;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.*;

/** Handler for <code>this</code> condition pointcut with a variable argument. */
public class ThisVar extends ThisAny {
    private Var var;

    public ThisVar(Var var,Position pos) {
	super(pos);
	this.var = var;
    }

    /** Get the pointcut variable that is bound by this
     *  <code>this</code> pointcut.
     */
    public Var getVar() {
	return var;
    }

    public String toString() {
	return "this("+var+")";
    }

    protected Residue matchesAt(WeavingEnv we,ContextValue cv) {
	Residue typeCheck=new CheckType(cv,we.getAbcType(var).getSootType());
	Residue bind=new Bind(cv,we.getWeavingVar(var));
	return AndResidue.construct(typeCheck,bind);
    }

}
