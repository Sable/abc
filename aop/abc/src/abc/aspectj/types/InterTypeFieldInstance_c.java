/*
 * Created on May 3, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package abc.aspectj.types;

import polyglot.util.Position;

import polyglot.types.ClassType;
import polyglot.types.TypeSystem;
import polyglot.types.Flags;
import polyglot.types.ReferenceType;
import polyglot.types.Type;

import polyglot.ext.jl.types.FieldInstance_c;

/**
 * @author oege
 * a FieldInstance that was introduced via an intertype declaration, recording its origin
 */
public class InterTypeFieldInstance_c extends FieldInstance_c implements InterTypeMemberInstance {
	
	protected ClassType origin;
	
	public InterTypeFieldInstance_c(TypeSystem ts, Position pos,
						ClassType origin,
						ReferenceType container,
						Flags flags, Type type, String name) {
		 super(ts, pos, container, flags, type, name);
	 	this.origin = origin;
	 }
	
	public ClassType origin() {
		return origin;
	}

}
