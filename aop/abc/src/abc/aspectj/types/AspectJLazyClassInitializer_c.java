
package arc.aspectj.types;

import polyglot.types.TypeSystem;
import polyglot.ext.jl.types.LazyClassInitializer_c;

public class AspectJLazyClassInitializer_c
	extends LazyClassInitializer_c
	implements AspectJLazyClassInitializer {
	
	public AspectJLazyClassInitializer_c(TypeSystem ts) {
		super(ts);
    }
    
	public void initPointcuts(AspectJParsedClassType ct) {
	}
	
	public void initAdvices(AspectJParsedClassType ct) {
	}

}
