package arc.aspectj.types;

import polyglot.types.ClassType;
import soot.javaToJimple.jj.types.JjTypeSystem;

public interface AspectJTypeSystem extends JjTypeSystem {
    
	public ClassType JoinPoint() ;
	
}
