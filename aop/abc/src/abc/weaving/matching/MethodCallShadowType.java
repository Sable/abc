package abc.weaving.matching;

/** A joinpoint shadow that applies at a method call
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public class MethodCallShadowType extends ShadowType {
    public ShadowMatch matchesAt(MethodPosition pos) {
	return MethodCallShadowMatch.matchesAt(pos);
    }

    private MethodCallShadowType() {
    }

    private static ShadowType v=new MethodCallShadowType();
    
    public static void register() {
	register(v);
    }
}
