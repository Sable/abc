
package polyglot.ext.aspectj.types;

import polyglot.types.MethodInstance;

public interface AdviceInstance extends MethodInstance {
	
	public static final int beforeKind = 0;
	public static final int afterKind = 1;
	public static final int aroundKind = 2;
	
	/**
	* The advice's kind.
	*/
	int kind();

	/**
	* Set the method's kind.
	*/
	AdviceInstance kind(int kind);
	
	/**
	 * The advice's pointcut 
	 */
	PointcutInstance pointcut();
	
	/**
	 * Set the advice's pointcut.
	 */
	AdviceInstance pointcut(PointcutInstance pci);
	
}
