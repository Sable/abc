
package abc.weaving.aspectinfo;

/**
 * @author oege
 *
 * Represent qualified references to "this"
 */

public class QualThis {
	MethodSig method;
	AbcClass target;
	AbcClass qualifier;
	
	public QualThis(MethodSig method, AbcClass target, AbcClass qualifier) {
		this.method = method;
		this.target = target;
		this.qualifier = qualifier;
	}
	
	public MethodSig getMethod() {
		return method;
	}
	
	public AbcClass getTarget() {
		return target;
	}
	
	public AbcClass getQualifier() {
		return qualifier;
	}
}
