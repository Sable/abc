package abc.weaving.aspectinfo;

import java.util.Hashtable;

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


    public boolean equivalent(Pointcut otherpc) {
	if (otherpc instanceof ThisType) {
	    AbcType othertype = ((ThisType)otherpc).getType();
	    return (othertype.equals(type));
	} else return false;
    }

	/* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.Pointcut#equivalent(abc.weaving.aspectinfo.Pointcut, java.util.Hashtable)
	 */
	public boolean equivalent(Pointcut otherpc, Hashtable renaming) {
		if (otherpc instanceof ThisType) {
			AbcType othertype = ((ThisType)otherpc).getType();
			return (othertype.equals(type));
		} else return false;
	}

}
