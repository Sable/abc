package arc.aspectj.types;

import java.util.List;
import polyglot.util.Position;

import polyglot.types.ClassType;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.Flags;
import polyglot.types.Type;

import arc.aspectj.ast.AdviceSpec;

import soot.javaToJimple.jj.types.JjTypeSystem;

public interface AspectJTypeSystem extends JjTypeSystem {
    
	public ClassType JoinPoint() ;
	public ClassType JoinPointStaticPart();
	public ClassType NoAspectBound();
	
	public MethodInstance adviceInstance(Position pos,
											ReferenceType container, Flags flags,
							Type returnType, String name,
							List argTypes, List excTypes, AdviceSpec spec);

	public MethodInstance pointcutInstance(Position pos,
											ReferenceType container, Flags flags,
											Type returnType, String name,
											List argTypes, List excTypes);
							
}
