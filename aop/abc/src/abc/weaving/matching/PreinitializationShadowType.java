package abc.weaving.matching;

/** A joinpoint shadow that applies at "preinitialization"
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public class PreinitializationShadowType extends ShadowType {
    public ShadowMatch matchesAt(MethodPosition pos) {
	return PreinitializationShadowMatch.matchesAt(pos);
    }

    private PreinitializationShadowType() {
    }

    private static ShadowType v=new PreinitializationShadowType();
    
    public static void register() {
	register(v);
    }
}
