
package abc.weaving.aspectinfo;

/**
 * represent super dispatch calls in target of intertype declarations
 * @author Oege de Moor
 */
public class SuperDispatch {
	private String name;
	private MethodSig methodsig;
	private AbcClass target;
	
	public String getName() {
		return name;
	}
	
	public MethodSig getMethodSig() {
		return methodsig;
	}
	
	public AbcClass getTarget() {
		return target;
	}
	
	public SuperDispatch(String name, MethodSig methodsig, AbcClass target) {
		this.name = name;
		this.methodsig = methodsig;
		this.target = target;
	}
}
