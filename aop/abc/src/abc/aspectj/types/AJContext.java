
package abc.aspectj.types;

import polyglot.types.ClassType;
import polyglot.types.Type;
import polyglot.types.Context;
import polyglot.types.MethodInstance;
import polyglot.types.VarInstance;
import polyglot.types.ReferenceType;

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
	
	/** the enclosing host scope, when in intertype **/
	AJContext hostScope();   
	
	/** traverse scopes until hostScope, looking for name;
	 * if it's found in the hostScope, return true, otherwise false.
	 */
	public boolean varInHost(String name);
	
	/** traverse scopes until hostScope, looking for name;
	 * if it's found in the hostScope, return true, otherwise false.
	 */
	public boolean methodInHost(String name);
	
	/** was enclosing intertype decl static? */
	boolean staticInterType();
	
	/** inner class inside intertype decl? */
	boolean nested(); 

	/** add all the members from the intertype host that are accessible */
	public void addITMembers(ReferenceType host);
}
