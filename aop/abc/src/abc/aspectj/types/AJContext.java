
package abc.aspectj.types;

import polyglot.types.ClassType;
import polyglot.types.Type;
import polyglot.types.Context;
import polyglot.types.MethodInstance;
import polyglot.types.VarInstance;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;

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
	
	/** was a field of this name introduced via the host? */
	public boolean varInHost(String name);
	
	/** was a method by this name introduced via the host? */
	public boolean methodInHost(String name);

	/** if varInHost(name), find the class that introduced the field by name (this can be an outer class
	 *  of the host. The result is in general a subtype of the field's container.
	 */
	public ClassType findFieldScopeInHost(String name);

	/** if methodInHost(name), find the class that introduced it (this can be an outer class
	 *  of the host. The result is in general a subtype of the method's container.
	 */
	public ClassType findMethodScopeInHost(String name) throws SemanticException;
	
	/** inner class inside intertype decl? */
	boolean nested(); 

	/** add all the members from the intertype host that are accessible */
	public AJContext addITMembers(ReferenceType host);
	
	/** was the ITD itself declared static? */
	public boolean explicitlyStatic();
	 
}
