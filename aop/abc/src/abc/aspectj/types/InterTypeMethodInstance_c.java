
package abc.aspectj.types;

import java.util.List;

import polyglot.ext.jl.types.MethodInstance_c;
import polyglot.types.Flags;
import polyglot.types.ReferenceType;
import polyglot.types.Type;
import polyglot.types.ClassType;
import polyglot.types.TypeSystem;
import polyglot.types.MethodInstance;

import polyglot.util.Position;
import polyglot.util.UniqueID;

/**
 * @author oege
 *
 * a method instance that was introduced via an intertype declaration
 */
public class InterTypeMethodInstance_c
	extends MethodInstance_c
	implements InterTypeMemberInstance {

	protected ClassType origin;
	protected MethodInstance mangled;
	
	public ClassType origin() {
		return origin;
	}

	
	/**
	 * create a methodInstance for an intertype dedclaration that can
	 * be traced back to its origin.
	 */
	public InterTypeMethodInstance_c(
		TypeSystem ts,
		Position pos,
		ClassType origin,
		ReferenceType container,
		Flags flags,
		Type returnType,
		String name,
		List formalTypes,
		List excTypes) {
		super(
			ts,
			pos,
			container,
			flags,
			returnType,
			name,
			formalTypes,
			excTypes);
		this.origin = origin;
//		prepare for later transformation to mangled form:
		if (flags.isPrivate() || flags.isPackage()){
			Flags newFlags = flags.clearPrivate().set(Flags.PUBLIC);
			String mangledName = UniqueID.newID("mangle$"+name);
			mangled = flags(newFlags).name(mangledName);
		} else mangled = this;  // no mangling
	}
	
	public MethodInstance mangled() {
		return mangled;
	}

}
