
package arc.aspectj.types;

import java.util.List;
import polyglot.types.ParsedClassType;

public interface AspectJParsedClassType 
       extends ParsedClassType, AspectJClassType {
	
	public void addPointcut(PointcutInstance pci);
	
	public void addAdvice(AdviceInstance ai);
	
	public List pointcuts();
	
	public List advices();
	
}
