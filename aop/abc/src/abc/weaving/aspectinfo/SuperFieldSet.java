
package abc.weaving.aspectinfo;

/**
 * @author oege
 *
 */
public class SuperFieldSet {
	private String name;
	private AbcClass target;
	private FieldSig fieldsig;
	
	public SuperFieldSet(FieldSig fieldsig,String name,AbcClass target) {
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
