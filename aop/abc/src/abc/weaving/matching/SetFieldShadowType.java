package abc.weaving.matching;

/** A joinpoint shadow that applies at a field set
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public class SetFieldShadowType extends ShadowType {
    public ShadowMatch matchesAt(MethodPosition pos) {
	return SetFieldShadowMatch.matchesAt(pos);
    }

    private SetFieldShadowType() {
    }

    private static ShadowType v=new SetFieldShadowType();
    
    public static void register() {
	register(v);
    }
}
