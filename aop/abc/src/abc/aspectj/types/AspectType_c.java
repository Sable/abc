
package abc.aspectj.types;

import polyglot.ext.jl.types.ParsedClassType_c;
import polyglot.frontend.Source;
import polyglot.types.LazyClassInitializer;
import polyglot.types.TypeSystem;


public class AspectType_c extends ParsedClassType_c implements AspectType {
	
	protected int perKind;

	public AspectType_c() {
		super();
	}

	
	public AspectType_c(
		TypeSystem ts,
		LazyClassInitializer init,
		Source fromSource, int perKind) {	
		super(ts, init, fromSource);
		this.perKind = perKind;
	}

	public int perKind() {
		if (perKind == PER_NONE && superType() instanceof AspectType)
			 return ((AspectType)superType()).perKind();
		else 
			return perKind;
	}
	
	public boolean perObject() {
		int per = perKind();
		return (per == PER_THIS || per == PER_TARGET);
	}
}
