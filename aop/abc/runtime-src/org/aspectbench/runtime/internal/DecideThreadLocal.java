package org.aspectbench.runtime.internal;

/** Decide whether or not it is possible to use the ThreadLocal implementation
 *  of cflow (at runtime)
 * @author Damien Sereni
 */
public class DecideThreadLocal {

	private static boolean useThreadLocal = set(); 
	
	private static boolean set() {
		// We are trying to work out whether we are on Java >= 1.2
		
		String jversion = System.getProperty("java.version");
		// Assume jversion is of the form x.y.<something> or x.y
		// for x, y ints. Otherwise, for safety default to false

		int firstDot = jversion.indexOf('.');
		if (firstDot == -1) return false;
		
		String major = jversion.substring(0, firstDot);
		
		// If the string ended at the first dot, it's weird and we give up
		if (firstDot == jversion.length()-1)
			return false;
		
		String rest = jversion.substring(firstDot+1);
		
		int secondDot = rest.indexOf('.');
		String minor = (firstDot == -1) ? 
				rest : 
				rest.substring(0, secondDot);

		try {
			int majorNumber = Integer.parseInt(major);
			int minorNumber = Integer.parseInt(minor);

			return (majorNumber > 1) || (majorNumber == 1 && minorNumber >= 2);
		} catch (NumberFormatException e) { return false; }
	}
	
	public static boolean ok() {
		return useThreadLocal;
	}
	
}
