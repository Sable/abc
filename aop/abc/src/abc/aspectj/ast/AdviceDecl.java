/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Oege de Moor
 * Copyright (C) 2004 Aske Simon Christensen
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

package abc.aspectj.ast;

import polyglot.ast.Formal;
import polyglot.ast.MethodDecl;
import polyglot.ast.Local;
import polyglot.types.CodeInstance;

import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.visit.TransformsAspectReflection;

/** @author Oege de Moor
 *  @author Aske Simon Christensen
 */

public interface AdviceDecl extends MethodDecl, 
                                    MakesAspectMethods, 
                                    TransformsAspectReflection
{

    /** generate a dummy MethodDecl for the proceed. Only applies to around advice. */
   	MethodDecl proceedDecl(AJNodeFactory nf,AJTypeSystem ts);
   	
   	/** generate a MethodDecl for the advice body */
   	MethodDecl methodDecl(AJNodeFactory nf,AJTypeSystem ts);
   	
   	/** register the use of "thisJoinPoint" etc.
   	 * @param n  test whether this local is "thisJoinPoint" etc();
   	 */
   	void joinpointFormals(Local n);
   	
   	/** does "thisJoinPoint" occur in the advice body? */
   	boolean hasJoinPoint();
   	
   	/** does "thisJoinPointStaticPart" occur in the advice body? */
   	boolean hasJoinPointStaticPart();
   	
   	/** register methods or constructors that are local to the advice, for later use in weaver
   	 * @param ci   code instance to register
   	 */
   	void localMethod(CodeInstance ci);

	/**
	 * For an after-returning or after-throwing advice, returns the formal that was used
	 * to bind the returned result / thrown exception (if any). In all other cases,
	 * <code>null</code> is returned. 
	 */
	public Formal getReturnThrowsFormal();
}
