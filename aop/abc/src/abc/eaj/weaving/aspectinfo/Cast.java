package abc.eaj.weaving.aspectinfo;

import soot.*;
import soot.jimple.*;

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
}
