
package abc.weaving.aspectinfo;

/**
 * represent dispatch methods to access fields in super class when referenced
 * from intertype declaration
 * @author Oege de Moor
 */
public class SuperFieldDispatch {
	private String name;
	private AbcClass target;
	private FieldSig fieldsig;
	
	public SuperFieldDispatch(FieldSig fieldsig,String name,AbcClass target) {
		this.fieldsig= fieldsig;
		this.name = name;
		this.target = target;
	}
	
	public String getName() {
		return name;
	}
	
	public AbcClass getTarget() {
		return target;
	}
	
	public FieldSig getFieldSig() {
		return fieldsig;
	}
}
