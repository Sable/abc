
package abc.aspectj.types;

import java.util.List;

import polyglot.util.InternalCompilerError;

import polyglot.ext.jl.types.MethodInstance_c;
import polyglot.types.Flags;
import polyglot.types.ReferenceType;
import polyglot.types.Type;
import polyglot.types.ClassType;
import polyglot.types.TypeSystem;
import polyglot.types.MethodInstance;
import polyglot.types.SemanticException;

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
	protected Flags origFlags;
	protected ClassType interfaceTarget; // for interface ITDs
	protected String identifier;
	
	public ClassType origin() {
		return origin;
	}
	
	public Flags origFlags() {
		return origFlags;
	}
	
	public String getIdentifier() {
		return identifier;
	}

	
	/**
	 * create a methodInstance for an intertype dedclaration that can
	 * be traced back to its origin.
	 */
	public InterTypeMethodInstance_c(
		TypeSystem ts,
		Position pos,
		String identifier,
		ClassType origin,
		ReferenceType container,
		Flags flags,
		Flags origFlags,
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
		this.identifier = identifier;
		this.origin = origin;
		this.origFlags = origFlags;
		if (container.toClass().flags().isInterface())
			interfaceTarget = container.toClass();
		else
			interfaceTarget = null;
//		prepare for later transformation to mangled form:
		if (flags.isPrivate() || flags.isPackage()){
			Flags newFlags = flags.clearPrivate().set(Flags.PUBLIC);
			String origName = origin.toString().replace('.','$');
			String mangledName = UniqueID.newID(origName+"$"+name);
			mangled = flags(newFlags).name(mangledName);
		} else mangled = this;  // no mangling
	}
	
	public ClassType interfaceTarget() {
		return interfaceTarget;
	}

	/** fix up the mangled instance to agree with super type */
	public void setMangle(AJTypeSystem ts) {
		if (container.superType() != null) {
			if (container.superType().toReference().hasMethod(this)) {
				MethodInstance superInstance = null;
				try {
				 superInstance = ts.findMethod(container.superType().toReference(),this.name(),this.formalTypes(),
				                               origin);
				} catch (SemanticException e) { throw new InternalCompilerError("could not find method"+e.getMessage()) ; }
				if (superInstance instanceof InterTypeMethodInstance_c)
				{	
					mangled = mangled.name(((InterTypeMethodInstance_c)superInstance).mangled().name());
				}
			    else {
			    	mangled = mangled.name(superInstance.name());
			    }
			}
		}
			
	}
	
	public MethodInstance mangled() {
		return mangled;
	}

}
