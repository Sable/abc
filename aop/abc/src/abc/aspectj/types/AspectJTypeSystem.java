package abc.aspectj.types;

import java.util.List;
import polyglot.util.Position;


import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.MethodInstance;
import polyglot.types.FieldInstance;
import polyglot.types.ReferenceType;
import polyglot.types.Flags;
import polyglot.types.Type;

import abc.aspectj.ast.AdviceSpec;

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
											
	public FieldInstance interTypeFieldInstance(
										  Position pos, ClassType origin,
										  ReferenceType container, Flags flags,
							              Type type, String name);
	
	public MethodInstance interTypeMethodInstance(Position pos,ClassType origin,
												ReferenceType container, Flags flags,
												Type returnType, String name,
												List argTypes, List excTypes);
	
	public ConstructorInstance interTypeConstructorInstance(Position pos,ClassType origin,
													ClassType container, Flags flags,
													List argTypes, List excTypes);
							              
}
