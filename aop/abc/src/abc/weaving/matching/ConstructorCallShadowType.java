package abc.weaving.matching;

/** A joinpoint shadow that applies at "new"+constructor call
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public class ConstructorCallShadowType extends ShadowType {
    public ShadowMatch matchesAt(MethodPosition pos) {
	return ConstructorCallShadowMatch.matchesAt(pos);
    }

    private ConstructorCallShadowType() {
    }

    private static ShadowType v=new ConstructorCallShadowType();
    
    public static void register() {
	register(v);
    }
}
