package abc.weaving.matching;

/** A joinpoint shadow that applies at "execution"
 *  @author Ganesh Sittampalam
 *  @date 29-Apr-04
 */
public class ExecutionShadowType extends ShadowType {
    public ShadowMatch matchesAt(MethodPosition pos) {
	return ExecutionShadowMatch.matchesAt(pos);
    }

    private ExecutionShadowType() {
    }

    private static ShadowType v=new ExecutionShadowType();
    
    public static void register() {
	register(v);
    }

}
