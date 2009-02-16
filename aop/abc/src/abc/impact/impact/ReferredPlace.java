/**
 * 
 */
package abc.impact.impact;

public enum ReferredPlace {
	/**
	 * within declaring class
	 */
	CLASS ("declaring class"),
	/**
	 * within declaring package
	 */
	PACKAGE ("declaring package"),
	/**
	 * within class or subclasses declaring protected member
	 */
	PROTECTED ("class or subclasses declaring protected member"),
	/**
	 * within other place
	 */
	OTHER ("other place");
	
	private String desc;
	
	ReferredPlace(String desc) {
		this.desc = desc;
	}
	
	public String toString() {
		return desc;
	}
}