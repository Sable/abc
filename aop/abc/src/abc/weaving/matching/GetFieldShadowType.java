package abc.weaving.matching;

/** A joinpoint shadow that applies at a field get
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public class GetFieldShadowType extends ShadowType {
    public ShadowMatch matchesAt(MethodPosition pos) {
	return GetFieldShadowMatch.matchesAt(pos);
    }

    private GetFieldShadowType() {
    }

    private static ShadowType v=new GetFieldShadowType();
    
    public static void register() {
	register(v);
    }
}
