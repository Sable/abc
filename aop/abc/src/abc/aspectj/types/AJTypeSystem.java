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
import polyglot.types.MemberInstance;
import polyglot.types.SemanticException;

import polyglot.frontend.Source;

import polyglot.ast.Typed;

import abc.aspectj.ast.AdviceSpec;

import soot.javaToJimple.jj.types.JjTypeSystem;

import polyglot.types.Context;

public interface AJTypeSystem extends JjTypeSystem {
    
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
										  Position pos, String id, ClassType origin,
										  ReferenceType container, Flags flags,
							              Type type, String name);
	
	public MethodInstance interTypeMethodInstance(Position pos, String id, ClassType origin,
												ReferenceType container, Flags flags, Flags oflags,
												Type returnType, String name,
												List argTypes, List excTypes);
	
	public ConstructorInstance interTypeConstructorInstance(Position pos, String id,ClassType origin,
													ClassType container, Flags flags,
													List argTypes, List excTypes);

	public boolean refHostOfITD(AJContext c, MemberInstance mi); 	
	public boolean refHostOfITD(AJContext c, Typed qualifier); 		
	
	public AspectType createAspectType(Source source, int perKind);
	
	// The normal isAccessible method inherited from JjTypeSystem is overridden so it always returns
	// true if called for a context that is a privileged aspect, and returns the value of
	// isAccessibleIgnorePrivileged otherwise.
	public boolean isAccessibleIgnorePrivileged(MemberInstance mi, Context ctc);	
	
	public void checkPointcutFlags(Flags f) throws SemanticException;			              
}
