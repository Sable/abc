package abc.eaj.weaving.matching;

import abc.weaving.matching.*;

/** A joinpoint shadow that applies at a cast
 */
public class CastShadowType extends ShadowType
{
    public ShadowMatch matchesAt(MethodPosition pos)
    {
        return CastShadowMatch.matchesAt(pos);
    }

    private CastShadowType() {}

    private static ShadowType v = new CastShadowType();

    public static void register()
    {
        register(v);
    }
}
