package arc.aspectj.types;

import java.util.List;
import polyglot.util.Position;
import polyglot.types.*;
import polyglot.frontend.Source;
import arc.aspectj.types.AspectJFlags;

import soot.javaToJimple.jj.types.JjTypeSystem_c;

public class AspectJTypeSystem_c 
       extends JjTypeSystem_c 
       implements AspectJTypeSystem {
    // TODO: implement new methods in AspectJTypeSystem.
    // TODO: override methods as needed from TypeSystem_c.
    
	protected ClassType JOINPOINT_;
	
    public ClassType JoinPoint()  { if (JOINPOINT_ != null) return JOINPOINT_;
									 return JOINPOINT_ = load("org.aspectj.lang.JoinPoint"); }

    

    // override to 
	public ParsedClassType createClassType(LazyClassInitializer init, Source fromSource) {
		   return new AspectJParsedClassType_c(this, init, fromSource);
	   }
	   
   /*	public LazyClassInitializer defaultClassInitializer() {
			   if (defaultClassInit == null) {
				   defaultClassInit = new AspectJLazyClassInitializer_c(this);
			   }

			   return defaultClassInit;
    } */
    
	protected final Flags ASPECT_FLAGS = AspectJFlags.privileged(AspectJFlags.aspect(ACCESS_FLAGS));
    
	public void checkTopLevelClassFlags(Flags f) throws SemanticException {
		    if (AspectJFlags.isAspect(f)) {
		       if (!f.clear(ASPECT_FLAGS).equals(Flags.NONE))
		       throw new SemanticException("Cannot declare aspect with flag(s) " +
		                                   f.clear(ASPECT_FLAGS));
		       return;
		    }
		    super.checkTopLevelClassFlags(f);
	}
			
			
	public AdviceInstance adviceInstance(Position pos,
										 ReferenceType container, 
										 Flags flags,
						                 Type returnType, 
						                 String name,
						                 List argTypes, 
						                 List excTypes,
						                 int kind,
						                 PointcutInstance pci) {
		   assert_(container);
		   assert_(returnType);
		   assert_(argTypes);
		   assert_(excTypes);
	   return new AdviceInstance_c(this, pos, container, flags,
					               returnType, name, argTypes, excTypes,kind,pci);
	   }

}
