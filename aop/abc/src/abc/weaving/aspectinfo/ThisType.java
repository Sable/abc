package abc.weaving.aspectinfo;

import polyglot.util.Position;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.*;

/** Handler for <code>this</code> condition pointcut with a type argument. */
public class ThisType extends ThisAny {
    private AbcType type;

    public ThisType(AbcType type,Position pos) {
	super(pos);
	this.type = type;
    }

    /** Get the type that is matched against <code>this</code>
     *  by this <code>this</code> pointcut.
     */
    public AbcType getType() {
	return type;
    }

    public String toString() {
	return "this("+type+")";
    }

    protected Residue matchesAt(WeavingEnv we,ContextValue cv) {
	return CheckType.construct(cv,type.getSootType());
    }
}
