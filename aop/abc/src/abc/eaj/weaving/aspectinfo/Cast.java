package abc.eaj.weaving.aspectinfo;

import java.util.Hashtable;

import soot.*;

import polyglot.util.Position;

import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;
import abc.weaving.residues.*;

import abc.eaj.weaving.matching.*;

/** Handler for <code>cast</code> shadow pointcut. */
public class Cast extends ShadowPointcut
{
    private TypePattern pattern;

    public Cast(TypePattern pattern, Position pos)
    {
        super(pos);
        this.pattern = pattern;
    }

    public TypePattern getPattern()
    {
        return pattern;
    }

    protected Residue matchesAt(ShadowMatch sm)
    {
        if (!(sm instanceof CastShadowMatch)) return null;
        Type cast_to = ((CastShadowMatch) sm).getCastType();

        if (!getPattern().matchesType(cast_to)) return null;
        return AlwaysMatch.v;
    }

    public String toString()
    {
        return "cast(" + pattern + ")";
    }

    public boolean equivalent(Pointcut otherpc) {
	if (otherpc instanceof Cast) {
	    return (pattern.equivalent(((Cast)otherpc).getPattern()));
	} else return false;
    }
	/* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.Pointcut#equivalent(abc.weaving.aspectinfo.Pointcut, java.util.Hashtable)
	 */
	public boolean equivalent(Pointcut otherpc, Hashtable renaming) {
		if (otherpc instanceof Cast) {
			return (pattern.equivalent(((Cast)otherpc).getPattern()));
		} else return false;
	}

}
