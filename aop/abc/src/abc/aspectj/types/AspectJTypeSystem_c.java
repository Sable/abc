package abc.aspectj.types;

import java.util.List;
import polyglot.util.Position;
import polyglot.types.*;
import polyglot.ast.Typed;


import abc.aspectj.ast.AdviceSpec;
import abc.aspectj.types.AspectJFlags;

import soot.javaToJimple.jj.types.JjTypeSystem_c;

public class AspectJTypeSystem_c 
       extends JjTypeSystem_c 
       implements AspectJTypeSystem {
    
    // importing the aspectJ runtime classes
	protected ClassType JOINPOINT_;
	
    public ClassType JoinPoint()  { if (JOINPOINT_ != null) return JOINPOINT_;
									 return JOINPOINT_ = load("org.aspectj.lang.JoinPoint"); }

	public ClassType JoinPointStaticPart() { 
		ClassType jp = JoinPoint();
		return jp.memberClassNamed("StaticPart");
	}
	
	protected ClassType NOASPECTBOUND_;
	
	public ClassType NoAspectBound() { if (NOASPECTBOUND_ !=null) return NOASPECTBOUND_;
										return NOASPECTBOUND_ = load("org.aspectj.lang.NoAspectBoundException");
	}
    
    // weeding out the wrong flags on aspects
	protected final Flags ASPECT_FLAGS = AspectJFlags.privileged(AspectJFlags.aspect(ACCESS_FLAGS.Abstract()));
 
	public void checkTopLevelClassFlags(Flags f) throws SemanticException {
		    if (AspectJFlags.isAspect(f)) {
		       if (!f.clear(ASPECT_FLAGS).equals(Flags.NONE))
		       throw new SemanticException("Cannot declare aspect with flag(s) " +
		                                   f.clear(ASPECT_FLAGS));
		       return;
		    }
		    super.checkTopLevelClassFlags(f);
	}
    		
	public MethodInstance adviceInstance(Position pos,
										ReferenceType container, Flags flags,
										Type returnType, String name,
										List argTypes, List excTypes, AdviceSpec spec) {

		   assert_(container);
		   assert_(returnType);
		   assert_(argTypes);
		   assert_(excTypes);
	   return new AdviceInstance_c(this, pos, container, flags,
					   returnType, name, argTypes, excTypes,spec);
	}	
   
	public MethodInstance pointcutInstance(Position pos,
											ReferenceType container, Flags flags,
											Type returnType, String name,
											List argTypes, List excTypes) {

			   assert_(container);
			   assert_(returnType);
			   assert_(argTypes);
			   assert_(excTypes);
		   return new PointcutInstance_c(this, pos, container, flags,
						   returnType, name, argTypes, excTypes);
		}	
	
	public FieldInstance interTypeFieldInstance(
		                                 	Position pos, ClassType origin,
										  	ReferenceType container, Flags flags,
							  				Type type, String name) {
		assert_(origin);
		assert_(container);
		assert_(type);
		return new InterTypeFieldInstance_c(this, pos, origin, container, flags, type, name);
	}
	
	public MethodInstance interTypeMethodInstance(Position pos,ClassType origin,
													ReferenceType container, Flags flags,
													Type returnType, String name,
													List argTypes, List excTypes){
		assert_(origin);
		assert_(container);
		assert_(returnType);
		assert_(argTypes);
		assert_(excTypes);
		return new InterTypeMethodInstance_c(this, pos, origin, container, flags,
		  										returnType, name, argTypes, excTypes);
														
	}
	
	public ConstructorInstance interTypeConstructorInstance(Position pos,ClassType origin,
														ClassType container, Flags flags,
														List argTypes, List excTypes) {
		assert_(origin);
		assert_(container);
		assert_(argTypes);
		assert_(excTypes);
		return new InterTypeConstructorInstance_c(this,pos,origin,container,flags,argTypes,excTypes);														
	}
		   
    protected boolean isAccessible(MemberInstance mi, ClassType ctc) {
        if (mi instanceof InterTypeMemberInstance) {
        	// the following code has been copied from TypeSystem_c.isAccessible
        	// the only change is the definition of target
			ReferenceType target = ((InterTypeMemberInstance) mi).origin(); 
														// accessibility of intertype declarations
		                                                // is with respect to origin, not container
			Flags flags = mi.flags();
			if (flags.isPublic()) return true;
			if (equals(target, ctc)) return true;
			if (! target.isClass()) return false;

			ClassType ctt = target.toClass();

			// If the current class and the target class are both in the
			// same class body, then protection doesn't matter, i.e.
			// protected and private members may be accessed. Do this by 
			// working up through ctc's containers.
			if (isEnclosed(ctc, ctt)) return true;                    
			if (isEnclosed(ctt, ctc)) return true;                        
			ClassType ctcContainer = ctc;
		while (!ctcContainer.isTopLevel()) {
			ctcContainer = ctcContainer.outer();
			if (isEnclosed(ctt, ctcContainer)) return true;                        
		};

		// Check for package level scope.
		// FIXME: protected too?
		if (ctt.package_() == null && ctc.package_() == null &&
			flags.isPackage())
			return true;

	
		if (ctt.package_() != null && ctt.package_().equals (ctc.package_()) &&
			(flags.isPackage() || flags.isProtected())) {
			return true;
		}
		
		return false; // ITDs cannot be protected
        }
    	else return super.isAccessible(mi,ctc);
    }
    
    public boolean refHostOfITD(AJContext c, Typed qualifier, MemberInstance mi) {
	   return !(!c.inInterType() 
					|| c.staticInterType()      // not so sure about this
					|| (c.nested() && qualifier == null) // and this
					|| (c.inInterType() && qualifier != null && 
						c.currentClass().hasEnclosingInstance(qualifier.type().toClass())));  
    }
    
	public Context createContext() {
	   return new AJContext_c(this);
	}
}
