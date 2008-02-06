/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Oege de Moor
 * Copyright (C) 2008 Eric Bodden
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

import java.util.List;

import polyglot.ast.Typed;
import polyglot.frontend.Source;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.Context;
import polyglot.types.FieldInstance;
import polyglot.types.Flags;
import polyglot.types.MemberInstance;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.Position;
import soot.javaToJimple.jj.types.JjTypeSystem;

/**
 * 
 * @author Oege de Moor
 * @author Eric Bodden
 *
 */
public interface AJTypeSystem extends JjTypeSystem {
    
	public ClassType JoinPoint() ;
	public ClassType JoinPointStaticPart();
	public ClassType NoAspectBound();

    public MethodInstance adviceInstance(Position pos,
                            ReferenceType container, Flags flags,
                            Type returnType, String name, List argTypes,
                            List excTypes, String signature);

	public MethodInstance pointcutInstance(Position pos,
											ReferenceType container, Flags flags, 
											Type returnType, String name,
											List argTypes, List excTypes);
											
	public FieldInstance interTypeFieldInstance(
										  Position pos, String id, ClassType origin,
										  ReferenceType container, Flags flags,
							              Type type, String name);
	
	public MethodInstance interTypeMethodInstance(Position pos, String id, ClassType origin,
												ReferenceType container, Flags flags, Flags oflags,
												Type returnType, String name,
												List argTypes, List excTypes);
	
	public ConstructorInstance interTypeConstructorInstance(Position pos, String id,ClassType origin,
													ClassType container, Flags flags,
													List argTypes, List excTypes);

	public boolean refHostOfITD(AJContext c, MemberInstance mi); 	
	public boolean refHostOfITD(AJContext c, Typed qualifier); 		
	
	public AspectType createAspectType(Source source, int perKind);
	
	// The normal isAccessible method inherited from JjTypeSystem is overridden so it always returns
	// true if called for a context that is a privileged aspect, and returns the value of
	// isAccessibleIgnorePrivileged otherwise.
	public boolean isAccessibleIgnorePrivileged(MemberInstance mi, Context ctc);	
	
	public void checkPointcutFlags(Flags f) throws SemanticException;	
	
	public PointcutInstance_c findPointCutNamed(ClassType ct, String name) throws SemanticException;
	
	/**
	 * Checks whether the flags f are allowed on an advice body.
	 * @param f some flags
	 * @throws SemanticException thrown in case a flag is not allowed
	 */
	public void checkAdviceBodyFlags(Flags f) throws SemanticException; 		              
}
