
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
	
/* intertype declarations */
	ClassType hostClass();
	
	/** enter an intertype decl */
	Context pushHost(ClassType ct, boolean declaredStatic); 
	
	/** in scope of an intertype decl? */
	boolean inInterType();      
	
	/** was a field of this name introduced via the host? */
	boolean varInHost(String name);
	
	/** was a method by this name introduced via the host? */
	boolean methodInHost(String name);

	/** if varInHost(name), find the class that introduced the field by name (this can be an outer class
	 *  of the host. The result is in general a subtype of the field's container.
	 */
	ClassType findFieldScopeInHost(String name);

	/** if methodInHost(name), find the class that introduced it (this can be an outer class
	 *  of the host. The result is in general a subtype of the method's container.
	 */
	ClassType findMethodScopeInHost(String name) throws SemanticException;
	
	/** inner class inside intertype decl? */
	boolean nested(); 

	/** add all the members from the intertype host that are accessible */
	AJContext addITMembers(ReferenceType host);
	
	/** was the ITD itself declared static? */
	boolean explicitlyStatic();
	
/* advice */

	AJContext pushAdvice(boolean isAround);
	
	void addProceed(MethodInstance proceedInstance);
	
	MethodInstance proceedInstance();
	
	boolean inAdvice();
	
/* declare error/warning */
 
	/** enter a declare warning/error declaration  */
	AJContext pushDeclare();

	/** are we in a declare decl? */
	boolean inDeclare(); 
	
	/** Get enclosing aspect, or null */
	public AspectType currentAspect();
	
}
