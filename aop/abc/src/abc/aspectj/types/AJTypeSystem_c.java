package abc.aspectj.types;

import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Iterator;

import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.Source;

import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.types.*;

import polyglot.ast.Typed;

import abc.aspectj.ast.AdviceSpec;
import abc.aspectj.types.AJFlags;

import soot.javaToJimple.jj.types.JjTypeSystem_c;

public class AJTypeSystem_c 
       extends JjTypeSystem_c 
       implements AJTypeSystem {
    
    // Make sure we only load from class files
    public void initialize(LoadedClassResolver loadedResolver, ExtensionInfo extInfo)
                           throws SemanticException {
	loadedResolver = new LoadedClassResolver(this, extInfo.getOptions().constructFullClasspath(),
						 extInfo.compiler().loader(), extInfo.version(), true);
	super.initialize(loadedResolver, extInfo);
	//this.systemResolver = new CachingResolver(loadedResolver, extInfo);
    }

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
	protected final Flags ASPECT_FLAGS = AJFlags.privilegedaspect(AJFlags.aspectclass(TOP_LEVEL_CLASS_FLAGS));
 
	public void checkTopLevelClassFlags(Flags f) throws SemanticException {
		    if (AJFlags.isAspectclass(f)) {
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
		                                 	Position pos, String id, ClassType origin,
										  	ReferenceType container, Flags flags,
							  				Type type, String name) {
		assert_(origin);
		assert_(container);
		assert_(type);
		return new InterTypeFieldInstance_c(this, pos, id, origin, container, flags, type, name);
	}
	
	public MethodInstance interTypeMethodInstance(Position pos,String id, ClassType origin,
													ReferenceType container, Flags flags, Flags oflags,
													Type returnType, String name,
													List argTypes, List excTypes){
		assert_(origin);
		assert_(container);
		assert_(returnType);
		assert_(argTypes);
		assert_(excTypes);
		return new InterTypeMethodInstance_c(this, pos, id, origin, container, flags, oflags,
		  										returnType, name, argTypes, excTypes);
														
	}
	
	public ConstructorInstance interTypeConstructorInstance(Position pos,String id, ClassType origin,
														ClassType container, Flags flags,
														List argTypes, List excTypes) {
		assert_(origin);
		assert_(container);
		assert_(argTypes);
		assert_(excTypes);
		return new InterTypeConstructorInstance_c(this,pos,id,origin,container,flags,argTypes,excTypes);														
	}
	
    public boolean isAccessible(MemberInstance mi, ClassType ctc) {
        if (mi instanceof InterTypeMemberInstance) {
        	
        	// the following code has been copied from TypeSystem_c.isAccessible
        	// the only change is the definition of target, which is normally mi.container()
        	// also we omit the class accessibility check
			ReferenceType target = ((InterTypeMemberInstance) mi).origin(); 
														// accessibility of intertype declarations
		                                                // is with respect to origin, not container
		    
			Flags flags = mi.flags();

			if (! target.isClass()) {
				// public members of non-classes are accessible;
				// non-public members of non-classes are inaccessible
				return flags.isPublic();
			}

			ClassType ctt = target.toClass();

			/*// Omitted: see new/PR569
			if (! classAccessible(ctt, ctc)) {
				return false;
			} */

			if (equals(ctt, ctc))
				return true;
				
			// If the current class and the target class are both in the
			// same class body, then protection doesn't matter, i.e.
			// protected and private members may be accessed. Do this by 
			// working up through ctc's containers.
			if (isEnclosed(ctc, ctt) || isEnclosed(ctt,ctc)) return true;                    
			                  
			ClassType ctcContainer = ctc;
			while (!ctcContainer.isTopLevel()) {
				ctcContainer = ctcContainer.outer();
				if (isEnclosed(ctt, ctcContainer)) return true;                        
			};

			// protected
			if (flags.isProtected()) {
				// If the current class is in a
				// class body that extends/implements the target class, then
				// protected members can be accessed. Do this by
				// working up through ctc's containers.
				if (descendsFrom(ctc, ctt)) {
					return true;
				}

				ctcContainer = ctc;
				while (!ctcContainer.isTopLevel()) {
					ctcContainer = ctcContainer.outer();
					if (descendsFrom(ctcContainer, ctt)) {
						return true;
					}
				}
			}

			return accessibleFromPackage(flags, ctt.package_(), ctc.package_());
        }
    	else return super.isAccessible(mi,ctc);
    }
    
    
  
    private boolean hostHasMember(AJContext c, MemberInstance mi) {
       if (mi instanceof FieldInstance)
       		return c.varInHost(((FieldInstance)mi).name());
       if (mi instanceof MethodInstance) {
       	// note: purely name-based, as this is prior to full disambiguation!
    		return c.methodInHost(((MethodInstance)mi).name());
    	}
    	return false;
    }
    
	/** Disambiguation of references to fields and methods that do *not*
	   * have an explicit target.
	   * 
	   * We need to determine whether the implicit receiver of this reference
	   * is the host of an intertype declaration, or whether it is a receiver (possibly
	   * an inner class) in an aspect. 
	   * 
	   * If the reference does not occur inside an intertype declaration, 
	   * it is treated according to the normal rules of Java.
	   * 
	   * If it inside an intertype declaration, we try to determine whether it might
	   * refer to the host. To this end, we look up the member name
	   * (and only the name, not taking into account the parameter types of a method)
	   * in the context. The lookup starts in the current scope, and continues until
	   * we reach the scope of the smallest enclosing intertype decl. It is in this
	   * scope that all the members of the host (that are visible from the aspect)
	   * are introduced. If the member name does occur in this scope we return
	   * true; in all other cases (if the name was found earlier, or it does not occur
	   * in the ITD scope) we return false.
	   */
    
    public boolean refHostOfITD(AJContext c, MemberInstance mi) {   
	   return c.inInterType() && hostHasMember(c,mi) ;
    }
    
        	
	/** Disambiguation of references of fields and references that do have an explicit
	 * target.
	 * 
	 * If we're not in the scope of an intertype declaration, the reference is treated
	 * according to normal Java rules.
	 * 
	 * If there is no explicit qualifier (like "A" in A.this or A.super), and we're not inside an
	 * inner class (inside an ITD), the special (this or super) refers to the host type.
	 * 
	 * If there is an explicit qualifier, we have to look for an enclosing instance of the appropriate
	 * type. 
	 */
	public boolean refHostOfITD(AJContext c, Typed qualifier) {
		if (!c.inInterType())
			return false;
		 if (qualifier == null)
			return !c.nested();
		else
			return c.hostClass().hasEnclosingInstance(qualifier.type().toClass());
	}
    
    
	public Context createContext() {
	   return new AJContext_c(this);
	}
	
	/** All flags allowed for a member class. */
	 protected final Flags MEMBER_CLASS_FLAGS = super.MEMBER_CLASS_FLAGS.set(AJFlags.ASPECTCLASS);
	 public void checkMemberClassFlags(Flags f) throws SemanticException {
			if (! f.clear(MEMBER_CLASS_FLAGS).equals(Flags.NONE)) {
			throw new SemanticException(
			"Cannot declare a member class with flag(s) " +
			f.clear(MEMBER_CLASS_FLAGS) + ".");
		}
		

		if (f.isStrictFP() && f.isInterface()) {
			throw new SemanticException("Cannot declare a strictfp interface.");
		}

		if (f.isFinal() && f.isInterface()) {
			throw new SemanticException("Cannot declare a final interface.");
		}

		checkAccessFlags(f);
	}


		/**
		   * Requires: all type arguments are canonical.
		   *
		   * Returns the fieldMatch named 'name' defined on 'type' visible from
		   * currrClass.  If no such field may be found, returns a fieldmatch
		   * with an error explaining why. name and currClass may be null, in which case
		   * they will not restrict the output.
		   * 
		   * This needs to be overridden for AspectJ because it is possible for
		   * the currClass to have multiple fields by the desired name, introduced
		   * by different aspects, that have different accessibility characteristics. 
		   **/
		
		  public FieldInstance findField(ReferenceType container, String name,
									 ClassType currClass) throws SemanticException {
			  	assert_(container);
			  	if (container == null) {
				  	throw new InternalCompilerError("Cannot access field \"" + name +
					  	"\" within a null container type.");
			  	}

			  	List /*FieldInstance*/ fis = findFieldInstances(container, name);
			  	// System.out.println("findField "+fis);
			 	List acceptable = new ArrayList();
			 
			  	for (Iterator fisit = fis.iterator(); fisit.hasNext() ; ) {
			  		FieldInstance fi= (FieldInstance) fisit.next();
			  		if (isAccessible(fi,currClass))
			  			acceptable.add(fi);
			  	}
			  
			  	if (acceptable.size() == 0){
				  	throw new SemanticException("Cannot access " + name + " in " + container + " from " + currClass + ".");
			 	 }
				if (acceptable.size() > 1) {
					throw new SemanticException("Ambiguous reference to " + name + " - multiple fields in " + container);
			  	}
			  	return (FieldInstance) acceptable.get(0);
		  	}

		  public List /*FieldInstance*/ findFieldInstances(ReferenceType container, String name)
										 throws SemanticException {
			  assert_(container);

			  	if (container == null) {
				  throw new InternalCompilerError("Cannot access field \"" + name +
					  "\" within a null container type.");
			  	}

				List result = new ArrayList();
			  	Stack s = new Stack();
			  	s.push(container);

			  	while (! s.isEmpty()) {
				  	Type t = (Type) s.pop();

			  		if (! t.isReference()) {
				  		throw new SemanticException("Cannot access a field in " +
				  											" non-reference type " + t + ".");
			  		}

				  	ReferenceType rt = t.toReference();

					result.addAll(fieldsNamed(rt.fields(),name));

					if (result.size() > 0)
						return result;
		
				  	if (rt.isClass()) {
					  	// Need to check interfaces for static fields.
					  	ClassType ct = rt.toClass();

					  	for (Iterator i = ct.interfaces().iterator(); i.hasNext(); ) {
						  	Type it = (Type) i.next();
						  	s.push(it);
					  	}
				  	}

				  	if (rt.superType() != null) {
					  	s.push(rt.superType());
				  	}
		  	}

			if (result.size() == 0)
		  			throw new NoMemberException(NoMemberException.FIELD, 
										  "Field \"" + name +
						  "\" not found in type \"" +
										  container + "\".");
			return result;
	}
	
		private List fieldsNamed(List fieldInstances, String name){
			List result = new ArrayList();
			for (Iterator fit = fieldInstances.iterator(); fit.hasNext(); ) {
				FieldInstance fi = (FieldInstance) fit.next();
				if (fi.name().equals(name)) 
					result.add(fi);
			}
			return result;
		}
	
		/**
		* Assert that <code>ct</code> implements all abstract methods required;
		* that is, if it is a concrete class, then it must implement all
		* interfaces and abstract methods that it or it's superclasses declare, and if 
		* it is an abstract class then any methods that it overrides are overridden 
		* correctly.
		*/
	   public void checkClassConformance(ClassType ct) throws SemanticException {
		   if (ct.flags().isInterface()) {
			   // don't need to check interfaces            
			   return;
		   }

		   // build up a list of superclasses and interfaces that ct 
		   // extends/implements that may contain abstract methods that 
		   // ct must define.
		   List superInterfaces = abstractSuperInterfaces(ct);

		   // check each abstract method of the classes and interfaces in
		   // superInterfaces
		   for (Iterator i = superInterfaces.iterator(); i.hasNext(); ) {
			   ReferenceType rt = (ReferenceType)i.next();
			   for (Iterator j = rt.methods().iterator(); j.hasNext(); ) {
				   MethodInstance mi = (MethodInstance)j.next();
				   if (!mi.flags().isAbstract()) {
					   // the method isn't abstract, so ct doesn't have to
					   // implement it.
					   continue;
				   }
				// FOLLOWING LINES ARE CHANGES FOR ASPECTJ:
				ClassType miContainer;
				if (mi instanceof InterTypeMemberInstance)
						miContainer = ((InterTypeMemberInstance) mi).origin();
				else
						miContainer = ct;
				// END OF CHANGES
				   boolean implFound = false;
				   ReferenceType curr = ct;
				   while (curr != null && !implFound) {
					   List possible = curr.methods(mi.name(), mi.formalTypes());
					   for (Iterator k = possible.iterator(); k.hasNext(); ) {
						   MethodInstance mj = (MethodInstance)k.next();
							//	NEWLY INSERTED FOR ASPECTJ:
							ClassType mjContainer;
							if (mj instanceof InterTypeMemberInstance)
									mjContainer = ((InterTypeMemberInstance) mj).origin();
							else
									mjContainer = mj.container().toClass(); // skip the test below, see new/introduceInnerInterfaceCP.java
							// NEXT LINE CHANGED FOR ASPECTJ:
						   if (!mj.flags().isAbstract() && 
							   ((isAccessible(mi, miContainer) && isAccessible(mj, miContainer)) || 
									   isAccessible(mi, mjContainer))) {
							   // The method mj may be a suitable implementation of mi.
							   // mj is not abstract, and either mj's container 
							   // can access mi (thus mj can really override mi), or
							   // mi and mj are both accessible from ct (e.g.,
							   // mi is declared in an interface that ct implements,
							   // and mj is defined in a superclass of ct).
                        
							   // If neither the method instance mj nor the method 
							   // instance mi is declared in the class type ct, then 
							   // we need to check that it has appropriate protections.
							   if (!equals(ct, mj.container()) && !equals(ct, mi.container())) {
								   try {
									   // check that mj can override mi, which
									   // includes access protection checks.
									   checkOverride(mj, mi);
								   }
								   catch (SemanticException e) {
									   // change the position of the semantic
									   // exception to be the class that we
									   // are checking.
									   throw new SemanticException(e.getMessage(),
										   ct.position());
								   }
							   }
							   else {
								   // the method implementation mj or mi was
								   // declared in ct. So other checks will take
								   // care of access issues
							   }
							   implFound = true;
							   break;
						   }
					   }

					   if (curr == mi.container()) {
						   // we've reached the definition of the abstract 
						   // method. We don't want to look higher in the 
						   // hierarchy; this is not an optimization, but is 
						   // required for correctness. 
						   break;
					   }
                
					   curr = curr.superType() ==  null ?
							  null : curr.superType().toReference();
				   }


				   // did we find a suitable implementation of the method mi?
				   if (!implFound && !ct.flags().isAbstract()) {
					   	if (mi instanceof InterTypeMemberInstance) {
					   		InterTypeMemberInstance itmi = (InterTypeMemberInstance) mi;
							throw new SemanticException(ct.fullName() + " should be " +
														   "declared abstract; it does not define " +
														   mi.signature() + ", which was injected into " +
														   rt.toClass().fullName() + " by aspect " +itmi.origin(), 
														   itmi.position());
					   	}
					   throw new SemanticException(ct.fullName() + " should be " +
							   "declared abstract; it does not define " +
							   mi.signature() + ", which is declared in " +
							   rt.toClass().fullName(), ct.position());
				   }
			   }
		   }
	   }	
		
	/** All flags allowed for a method. */
	protected final Flags AJ_METHOD_FLAGS = AJFlags.intertype(AJFlags.interfaceorigin(METHOD_FLAGS));

	public void checkMethodFlags(Flags f) throws SemanticException {
		  if (! f.clear(AJ_METHOD_FLAGS).equals(Flags.NONE)) {
		  throw new SemanticException(
		  "Cannot declare method with flags " +
		  f.clear(METHOD_FLAGS) + ".");
	  		}

		  if (f.isAbstract() && f.isPrivate() && ! AJFlags.isIntertype(f)) {
		  throw new SemanticException(
		  "Cannot declare method that is both abstract and private.");
		  }

		  if (f.isAbstract() && f.isStatic()) {
		  throw new SemanticException(
		  "Cannot declare method that is both abstract and static.");
		  }

		  if (f.isAbstract() && f.isFinal()) {
		  throw new SemanticException(
		  "Cannot declare method that is both abstract and final.");
		  }

		  if (f.isAbstract() && f.isNative()) {
		  throw new SemanticException(
		  "Cannot declare method that is both abstract and native.");
		  }

		  if (f.isAbstract() && f.isSynchronized()) {
			throw new SemanticException(
			"Cannot declare method that is both abstract and synchronized.");
		  }
	
			if (f.isAbstract() && f.isStrictFP()) {
			throw new SemanticException(
			"Cannot declare method that is both abstract and strictfp.");
			}

			checkAccessFlags(f); 
			  
		  }

	public List findAcceptableMethods(ReferenceType container, String name,
											List argTypes, ClassType currClass) throws SemanticException {
		return super.findAcceptableMethods(container,name,argTypes,currClass);
	}
	
	public final AspectType createAspectType(Source fromSource, int perKind) {
		  return createAspectType(defaultClassInitializer(), fromSource, perKind);
	}

	public AspectType createAspectType(LazyClassInitializer init, Source fromSource, int perKind) {
		  return new AspectType_c(this, init, fromSource, perKind);
	}

}
