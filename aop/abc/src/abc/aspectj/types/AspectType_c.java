
package abc.aspectj.types;

import polyglot.ext.jl.types.ParsedClassType_c;
import polyglot.frontend.Source;
import polyglot.types.LazyClassInitializer;
import polyglot.types.TypeSystem;

import abc.aspectj.visit.AccessorMethods;

/**
 * 
 * @author Oege de Moor
 *
 */
public class AspectType_c extends ParsedClassType_c implements AspectType {
	
	protected int perKind;
	
	protected AccessorMethods accessorMethods;

	public AspectType_c() {
		super();
		this.accessorMethods = new AccessorMethods();
	}

	
	public AspectType_c(
		TypeSystem ts,
		LazyClassInitializer init,
		Source fromSource, int perKind) {	
		super(ts, init, fromSource);
		this.perKind = perKind;
		this.accessorMethods = new AccessorMethods();
	}

	public int perKind() {
		if (perKind == PER_NONE)
		   	if (superType() instanceof AspectType)
			 	return ((AspectType)superType()).perKind();
			else 
				return AspectType.PER_SINGLETON;
		else 
			return perKind;
	}
	
	public boolean perObject() {
		int per = perKind();
		return (per == PER_THIS || per == PER_TARGET);
	}
	
	public AccessorMethods getAccessorMethods() {
	    return accessorMethods;
	}
}
