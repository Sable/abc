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

/*
 * Created on Jul 13, 2004
 *
 */
package abc.aspectj.visit;

import polyglot.util.Position;

import polyglot.ast.NodeFactory;

import polyglot.frontend.Job;

import polyglot.types.Flags;
import polyglot.types.TypeSystem;
import polyglot.types.SemanticException;
import polyglot.types.ClassType;
import polyglot.types.ParsedClassType;
import polyglot.types.Named;
import polyglot.types.CachingResolver;

import polyglot.visit.TypeBuilder;

import abc.aspectj.types.AJTypeSystem;

/**
 * @author Oege de Moor
 *
 */
public class AJTypeBuilder extends TypeBuilder {

	/**
	 * @param job
	 * @param ts
	 * @param nf
	 */
	public AJTypeBuilder(Job job, TypeSystem ts, NodeFactory nf) {
		super(job, ts, nf);
		// TODO Auto-generated constructor stub
	}

	/** cloned from TypeBuilder.pushClass */
	public AJTypeBuilder pushAspect(Position pos, Flags flags, String name,int perKind)
		 throws SemanticException {

		 ParsedClassType t = newAspect(pos, flags, name,perKind);
		 return (AJTypeBuilder) pushClass(t);
	 }
	 
	/** cloned from TypeBuilder.newClass */
	protected ParsedClassType newAspect(Position pos, Flags flags, String name, int perKind) 
	  throws SemanticException{
		AJTypeSystem ts = (AJTypeSystem) typeSystem();

		ParsedClassType ct = ts.createAspectType(this.job.source(),perKind);

		if (inCode) {
				ct.kind(ClassType.LOCAL);
			ct.outer(currentClass());
			ct.flags(flags);
			ct.name(name);
			ct.position(pos);

			if (currentPackage() != null) {
				ct.package_(currentPackage());
			}

			return ct;
		}
		else if (currentClass() != null) {
				ct.kind(ClassType.MEMBER);
			ct.outer(currentClass());
			ct.flags(flags);
			ct.name(name);
			ct.position(pos);

			currentClass().addMemberClass(ct);

			if (currentPackage() != null) {
				ct.package_(currentPackage());
			}

				// if all the containing classes for this class are member
				// classes or top level classes, then add this class to the
				// parsed resolver.
				ClassType container = ct.outer();
				boolean allMembers = (container.isMember() || container.isTopLevel());
				while (container.isMember()) {
					container = container.outer();
					allMembers = allMembers && 
							(container.isMember() || container.isTopLevel());
				}
				if (allMembers) {
					typeSystem().parsedResolver().addNamed(
							typeSystem().getTransformedClassName(ct), ct);
				}

			return ct;
		}
		else {
				ct.kind(ClassType.TOP_LEVEL);
			ct.flags(flags);
			ct.name(name);
			ct.position(pos);

			if (currentPackage() != null) {
				ct.package_(currentPackage());
			}

			Named dup = ((CachingResolver) typeSystem().systemResolver()).check(ct.fullName());

		   if (dup != null) {
			   throw new SemanticException("Duplicate class \"" +
										   ct.fullName() + "\".", pos);
		   }
			typeSystem().parsedResolver().addNamed(ct.fullName(), ct);
			((CachingResolver) typeSystem().systemResolver()).addNamed(ct.fullName(), ct);
			
			return ct;
		}
		}

}
