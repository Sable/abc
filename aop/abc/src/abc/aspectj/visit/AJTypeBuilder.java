/*
 * Created on Jul 13, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
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

import polyglot.visit.TypeBuilder;

import abc.aspectj.types.AJTypeSystem;

/**
 * @author oege
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
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
	protected ParsedClassType newAspect(Position pos, Flags flags, String name, int perKind) {
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

			typeSystem().parsedResolver().addNamed(ct.fullName(), ct);

			return ct;
		}
		}

}
