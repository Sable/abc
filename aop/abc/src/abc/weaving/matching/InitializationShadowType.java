package abc.weaving.matching;

/** A joinpoint shadow that applies at "initialization"
 *  @author Ganesh Sittampalam
 *  @date 29-Apr-04
 */
public class InitializationShadowType extends ShadowType {
    public ShadowMatch matchesAt(MethodPosition pos) {
	return InitializationShadowMatch.matchesAt(pos);
    }

    private InitializationShadowType() {
    }

    private static ShadowType v=new InitializationShadowType();
    
    public static void register() {
	register(v);
    }
}
