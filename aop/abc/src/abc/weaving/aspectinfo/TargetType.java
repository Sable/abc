package abc.weaving.aspectinfo;

import java.util.Hashtable;

import polyglot.util.Position;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.*;

/** Handler for <code>target</code> condition pointcut with a type argument. */
public class TargetType extends TargetAny {
    private AbcType type;

    public TargetType(AbcType type,Position pos) {
	super(pos);
	this.type = type;
    }

    /** Get the type that is matched against the target
     *  by this <code>target</code> pointcut.
     */
    public AbcType getType() {
	return type;
    }

    public String toString() {
	return "target("+type+")";
    }

    protected Residue matchesAt(WeavingEnv we,ContextValue cv) {
	return CheckType.construct(cv,type.getSootType());
    }

	/* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.Pointcut#equivalent(abc.weaving.aspectinfo.Pointcut, java.util.Hashtable)
	 */
	public boolean equivalent(Pointcut otherpc, Hashtable renaming) {
		if (otherpc instanceof TargetType) {
			AbcType othertype = ((TargetType)otherpc).getType();
			return (othertype.equals(type));
		} else return false;
	}

}
