
package arc.aspectj.types;

import polyglot.types.LazyClassInitializer;

public interface AspectJLazyClassInitializer extends LazyClassInitializer {

	public void initPointcuts(AspectJParsedClassType ct);
	
	public void initAdvices(AspectJParsedClassType ct);
	
}
