
package abc.aspectj.types;

import polyglot.types.ParsedClassType;
import abc.aspectj.visit.AccessorMethods;

/**
 * 
 * @author Oege de Moor
 *
 */
public interface AspectType extends ParsedClassType {
	public static int PER_NONE = 0;
	public static int PER_SINGLETON = 1;
	public static int PER_THIS = 2;
	public static int PER_TARGET = 3;
	public static int PER_CFLOW = 4;
	public static int PER_CFLOWBELOW = 5;
	
	int perKind();
	
	boolean perObject();
	
	public AccessorMethods getAccessorMethods();
}
