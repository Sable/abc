/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Oege de Moor
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package abc.aspectj.types;

import java.util.Collection;
import polyglot.types.ClassType;
import polyglot.types.Type;
import polyglot.types.Context;
import polyglot.types.MethodInstance;
import polyglot.types.VarInstance;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;

/**
 * @author Oege de Moor
 */
public interface AJContext extends Context {
	
/* intertype declarations */
	ClassType hostClass();
	
	Context pushAspect(AspectType ct);
	
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

/* other pointcut stuff */

    /** find the class that introduced a pointcut by the given name */
    ClassType findPointcutScope(String name) throws SemanticException;
    
	/** mark entry of cflow pointcut expr */
	AJContext pushCflow(Collection mustBind);
	
	/** mark entry of if pointcut expr */
	AJContext pushIf();
	
	/** inside a cflow? */
	boolean inCflow();
	
	/** get the names of variables that are bound in the smallest enclosing cflow */
	Collection getCflowMustBind();
	
	/** inside an if pointcut? */
	boolean inIf();
}
