package abc.weaving.matching;

/** A joinpoint shadow that applies at an exception handler
 *  @author Ganesh Sittampalam
 *  @date 29-Apr-04
 */
public class HandlerShadowType extends ShadowType {
    public ShadowMatch matchesAt(MethodPosition pos) {
	return HandlerShadowMatch.matchesAt(pos);
    }

    private HandlerShadowType() {
    }

    private static ShadowType v=new HandlerShadowType();
    
    public static void register() {
	register(v);
    }
}
