
package abc.aspectj.types;

import java.util.List;

import polyglot.ext.jl.types.ConstructorInstance_c;
import polyglot.types.ClassType;
import polyglot.types.Flags;
import polyglot.types.TypeSystem;
import polyglot.util.Position;

/**
 * @author Oege de Moor
 *
 * A constructor that was introduced via an intertype declaration.
 */

public class InterTypeConstructorInstance_c
	extends ConstructorInstance_c
	implements InterTypeMemberInstance {

	protected ClassType origin;
	
	public ClassType origin() {
		return origin;
	}

	/** create a constructor that can be traced back to the aspect
	 * that introduced it.
	 * 
	 */
	public InterTypeConstructorInstance_c(
		TypeSystem ts,
		Position pos,
		ClassType origin,
		ClassType container,
		Flags flags,
		List formalTypes,
		List excTypes) {
		super(ts, pos, container, flags, formalTypes, excTypes);
		this.origin = origin;
	}

}
