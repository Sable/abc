
package abc.aspectj.types;

import polyglot.types.ClassType;
import polyglot.types.Type;
import polyglot.types.Context;

/**
 * @author Oege de Moor
 *
 * handle this and super in intertype method decls
 */
public interface AJContext extends Context {
	
	ClassType hostClass();
	
	/** enter an intertype decl */
	Context pushHost(ClassType ct, boolean declaredStatic); 
	
	/** in scope of an intertype decl? */
	boolean inInterType();         
	
	/** was enclosing intertype decl static? */
	boolean staticInterType();
	
	/** inner class inside intertype decl? */
	boolean nested(); 
	            
}
