/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Oege de Moor
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package abc.weaving.weaver;
import soot.*;
import soot.util.*;
import soot.jimple.*;
import soot.tagkit.*;
import java.util.*;

import polyglot.util.UniqueID;
import polyglot.util.InternalCompilerError;

import abc.weaving.aspectinfo.*;
import abc.aspectj.types.AspectType;

/**
 * 
 * @author Oege de Moor
 *
 */
public class IntertypeAdjuster {
	
	public void adjust() {
    // Generate Soot signatures for intertype methods and fields
 
    // 	weave in intertype methods
        for( Iterator imdIt = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getIntertypeMethodDecls().iterator(); imdIt.hasNext(); ) {
            final IntertypeMethodDecl imd = (IntertypeMethodDecl) imdIt.next();
            addMethod( imd );
        }
   // 	weave in intertype constructors
		for( Iterator imdIt = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getIntertypeConstructorDecls().iterator(); imdIt.hasNext(); ) {
		 	final IntertypeConstructorDecl icd = (IntertypeConstructorDecl) imdIt.next();
		 	addConstructor( icd );
	 	} 
   	//	weave in intertype fields
        for( Iterator ifdIt = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getIntertypeFieldDecls().iterator(); ifdIt.hasNext(); ) {
            final IntertypeFieldDecl ifd = (IntertypeFieldDecl) ifdIt.next();
            addField( ifd );
        }
        
        // accessors for super members are now stored in AspectType
        // TODO: In the original implementation, qualified special accesses were the first thing
        // to be processed. Check if this is essential.
        for(Iterator asIt = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getAspects().iterator(); asIt.hasNext(); ) {
            Aspect a = (Aspect) asIt.next();
            AspectType at =(AspectType) a.getInstanceClass().getPolyglotType();
            at.getAccessorMethods().addAllSootMethods();
        }
    }
    
/* intertype method declarations */	
	
	private Map /* (SootMethod, SootClass) */  interfaceTarget;
	private Map /* (ClassMember, Aspect) */ intertype;
	private FastHierarchy hierarchy;
	
	public IntertypeAdjuster() {
		interfaceTarget = new HashMap();
		intertype = new HashMap();
		hierarchy = soot.Scene.v().getOrMakeFastHierarchy();
	}
	
	SootClass interfaceTarget(SootMethod sm) {
		return (SootClass) interfaceTarget.get(sm);
	}
	
	boolean fromInterface(SootMethod sm) {
		return (interfaceTarget(sm) != null);
	}
	
	Aspect origin(ClassMember sm) {
		return (Aspect) intertype.get(sm);
	}
	
	boolean isIntertype(ClassMember sm) {
		return (origin(sm) != null);
	}
	
	boolean descendsfrom(SootClass sc1, SootClass sc2) {
		return !(sc1.equals(sc2)) && 
		           (hierarchy.getAllImplementersOfInterface(sc2).contains(sc1) ||
		           // soot.Scene.v().getFastHierarchy().getSubclassesOf(sc2).contains(sc1));
				   transextends(sc1,sc2));
	}
	
	
	  
	boolean transextends(SootClass sc1, SootClass sc2) {
		if (sc1.hasSuperclass()) {
			return sc2.equals( sc1.getSuperclass())  || transextends(sc1.getSuperclass(),sc2);
		} else return false;
	}
	
	private boolean overrideITDmethod(SootClass pht, 
															 SootMethod mi) {
		boolean skipped = false;
		if (pht.declaresMethod(mi.getName(),mi.getParameterTypes())) {
			// System.out.println("it has the method already");
			SootMethod minst = pht.getMethod(mi.getName(),mi.getParameterTypes());
			if (zapsmethod(pht,mi,minst)){   
					pht.removeMethod(minst);
					pht.addMethod(mi);
			} else if (zapsmethod(pht,minst,mi)) {	
					skipped = true;
					}
				else { System.out.println("minst="+minst+" of origin "+origin(minst));
						    System.out.println("mi of origin " + origin(mi));
					        throw new InternalCompilerError("introduction of "+mi.getName()+
                                            " conflicts with an existing class member of " +pht); } 
			}
		else pht.addMethod(mi); 
		return !skipped;
	}
	
	
	private boolean isSubInterface(SootClass sc1, SootClass sc2) {
		Stack worklist = new Stack();
		HashSet visited = new HashSet();
		worklist.push(sc1);
		while (! worklist.isEmpty()) {
			SootClass sc = (SootClass) worklist.pop();
			if (visited.contains(sc)) continue;
			visited.add(sc);
			if (sc.equals(sc2)) return true;
			worklist.addAll(sc.getInterfaces());
		}
		return false;
	}
	
	
	boolean zapsmethod(SootClass pht, SootMethod mi1,SootMethod mi2) {
		if (!(Modifier.isAbstract(mi1.getModifiers())) && Modifier.isAbstract(mi2.getModifiers()))
			return true;
		// was mi2 then mi1
		if (pht.isInterface() && fromInterface(mi1)) return true;
		if (!fromInterface(mi1) && fromInterface(mi2)) return true;
		if (!(isIntertype(mi1)  && isIntertype(mi2))) return false;
		if (fromInterface(mi1) && fromInterface(mi2) &&
			 isSubInterface(interfaceTarget(mi1),interfaceTarget(mi2)))
			return true;
		return abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getPrecedence(origin(mi1),origin(mi2)) == GlobalAspectInfo.PRECEDENCE_FIRST;    
	}
	
	private void addMethod( IntertypeMethodDecl imd ) {
		// System.out.println("add method "+imd.getTarget() + "from "+imd.getAspect() +
		//                   " and implementation " + imd.getImpl());
		// System.out.println(imd.getTarget().getDeclaringClass().getSootClass().getMethods());
		SootMethod implMethod = addImplMethod(imd);
		addTargetMethod(imd,implMethod);
	}
	
	/** return the relevant sootMethod that implements this IMD.
	 *  the imd implementation stub has been added by the front end already,
	 *  so we only look it up.
	 */
	private SootMethod addImplMethod( IntertypeMethodDecl imd ) {
		
		MethodSig method = imd.getImpl();
        
		SootClass sc = method.getDeclaringClass().getSootClass();
		Type retType = method.getReturnType().getSootType();
		List parms = new ArrayList();
		for( Iterator formalIt = method.getFormals().iterator(); formalIt.hasNext(); ) {
			final AbcType formalType = ((Formal) formalIt.next()).getType();
			parms.add(formalType.getSootType());
		}
 
		int modifiers = method.getModifiers();
		
        if (!Modifier.isAbstract(modifiers)) 
           return sc.getMethod(method.getName(),parms);
        else return null;
         
	}
	
    private void addTargetMethod( IntertypeMethodDecl imd, SootMethod implMethod) {
        MethodSig method = imd.getTarget();
        // System.out.println("target method for "+imd);
        SootClass sc = method.getDeclaringClass().getSootClass();
		 if( sc.isInterface() ) {
		 		MethodSig itf = new MethodSig(method.getModifiers() | Modifier.ABSTRACT, 
		 		                              method.getDeclaringClass(),
		 		                              method.getReturnType(), 
		 		                              method.getName(), 
		 		                              method.getFormals(),
		 		                              method.getExceptions(), 
		 		                              method.getPosition());
		 		 // System.out.println("creating target method in "+sc);
		 		createTargetMethod(implMethod,itf,sc,imd.getAspect(),sc,imd.getOrigName());
		 		Set implementors = hierarchy.getAllImplementersOfInterface(sc);
			   for( Iterator childClassIt = implementors.iterator(); childClassIt.hasNext(); ) {
				   final SootClass childClass = (SootClass) childClassIt.next();
				   if( childClass.isInterface() ) continue;
				   if( childClass.hasSuperclass() 
				   && implementors.contains(childClass.getSuperclass()) )
					   continue;

					// System.out.println("creating interface impl target method in "+childClass);
                   createTargetMethod(implMethod,method,childClass,imd.getAspect(),sc,imd.getOrigName());
				  
			   }
		   } else createTargetMethod(implMethod,method,sc, imd.getAspect(),null,imd.getOrigName());
    }
    
    
  

	private void createTargetMethod(
		SootMethod implMethod,
		MethodSig method,
		SootClass sc,
		Aspect origin,
		SootClass intftarget,
		String originalName) {	
			// System.out.println("added method "+ method.getName() + " to class " + sc);
		        Type retType = method.getReturnType().getSootType();
		        List parms = new ArrayList();
		        for( Iterator formalIt = method.getFormals().iterator(); formalIt.hasNext(); ) {
		            final AbcType formalType = ((Formal) formalIt.next()).getType();
		            parms.add(formalType.getSootType());
		        }
		        if ( ! Modifier.isStatic(method.getModifiers()) )
		        	parms.remove(0); // drop the "this" parameter
		
		        int modifiers = method.getModifiers();
		        modifiers |= Modifier.PUBLIC;
		        modifiers &= ~Modifier.PRIVATE;
		        modifiers &= ~Modifier.PROTECTED;
			    // System.out.println("added method "+ method.getName() + " with modifiers " + Modifier.toString(modifiers) +  " to class " + sc);    
		       
		        // Create the method
		        SootMethod sm = new SootMethod( 
		                method.getName(),
		                parms,
		                retType,
		                modifiers );
		
		        for( Iterator exceptionIt = method.getSootExceptions().iterator(); exceptionIt.hasNext(); ) {
		
		            final SootClass exception = (SootClass) exceptionIt.next();
		            sm.addException( exception );
		        }
		
				if (intftarget != null)
					interfaceTarget.put(sm,intftarget);
				intertype.put(sm,origin);
		//			Add method to the class
		//		sc.addMethod(sm);	
		        if (!overrideITDmethod(sc,sm))
		        	return;
		        			
				if (!Modifier.isAbstract(modifiers)) {
			/* generate call to implementation: impl(this,arg1,arg2,...,argn) */	
			    //create a body
				 	Body b = Jimple.v().newBody(sm); sm.setActiveBody(b);
				 	Chain ls = b.getLocals();
				 	PatchingChain ss = b.getUnits();
			    // argument set-up
				    List args = new LinkedList();
			    //	the first parameter of the impl is "this : TargetType"
					RefType rt = sc.getType();
					if (!Modifier.isStatic(modifiers)) {
						ThisRef thisref = Jimple.v().newThisRef(rt);
						Local v = Jimple.v().newLocal("this$",rt); ls.add(v);
						IdentityStmt thisStmt = soot.jimple.Jimple.v().newIdentityStmt(v,thisref); ss.add(thisStmt);
						args.add(v);
					}
				// add references to the other parameters
					int index = 0;
					for (Iterator formals=parms.iterator(); formals.hasNext(); ) {
						final Type formalType = (Type) formals.next();
						Local p = Jimple.v().newLocal("$param"+index,formalType); ; ls.add(p);
						ParameterRef pr = Jimple.v().newParameterRef(formalType,index);
						IdentityStmt prStmt = soot.jimple.Jimple.v().newIdentityStmt(p, pr); ss.add(prStmt);
						args.add(p);
						index++;
					}
				// now invoke the implementation in the originating aspect
					InvokeExpr ie = Jimple.v().newStaticInvokeExpr
					    (implMethod.makeRef(),args);
				// if this is a void returntype, create call followed by return
				// otherwise return the value directly
					if (retType.equals(VoidType.v())) {
						InvokeStmt stmt1 = Jimple.v().newInvokeStmt(ie);
						ReturnVoidStmt stmt2 = Jimple.v().newReturnVoidStmt();
						ss.add(stmt1); ss.add(stmt2);
					} else {
						Local r = Jimple.v().newLocal("$result",retType); ls.add(r);
						AssignStmt rStmt = soot.jimple.Jimple.v().newAssignStmt(r, ie); ss.add(rStmt);
						ReturnStmt stmt = Jimple.v().newReturnStmt(r); 
						ss.add(stmt);
					}
				}
		
				// System.out.println("added target method "+method);
				// This is a stub for an intertype method decl
				MethodCategory.register(sm, MethodCategory.INTERTYPE_METHOD_DELEGATOR);
				// System.out.println("registered "+sm+" with original name "+originalName);
				MethodCategory.registerRealNameAndClass(sm, method.getModifiers(), originalName, method.getDeclaringClass(),
									0,0); //FIXME: Extra formals?
	}
		
		
	private Map fieldToITD = new HashMap();       /* maps a field to the IFD that introduced it               */
	private Map fieldITtargets = new HashMap(); /* maps IFDs to the set of targets where it ended up */
		
	public  boolean overrideITDfield(SootClass pht, SootField fi, IntertypeFieldDecl origin) {
			boolean skipped = false;
			fieldToITD.put(fi,origin);
			intertype.put(fi,origin.getAspect());
			if (pht.declaresFieldByName(fi.getName())) {
				SootField finst = pht.getFieldByName(fi.getName());
				if (zapsfield(fi,finst)){   
						pht.removeField(finst);
						IntertypeFieldDecl ifd = (IntertypeFieldDecl) fieldToITD.get(finst);
						if (ifd != null) ((Set) fieldITtargets.get(ifd)).remove(pht);
						pht.addField(fi);
					}
					else if (zapsfield(finst,fi)) {	
						skipped = true;
						}
					else { // pht.addField(fi); 
								throw new InternalCompilerError("introduced ambiguous field");
							}
			} else pht.addField(fi); 
			if (!skipped) {
				Set s = (Set)  fieldITtargets.get(origin);
				if (s==null) {
					s = new HashSet();
					fieldITtargets.put(origin,s);
					MethodCategory.registerRealNameAndClass(fi,MethodCategory.getModifiers(origin.getTarget()),
					                                        MethodCategory.getName(origin.getTarget()),
					                                        AbcFactory.AbcClass(MethodCategory.getClass(origin.getTarget())));
				}
				s.add(pht);
			}
			return !skipped;
		}
	
		
	boolean zapsfield(SootField mi1,SootField mi2) {
			if (!(isIntertype(mi1) && isIntertype(mi2)))
				return false;
			return abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getPrecedence(origin(mi1),origin(mi2)) == GlobalAspectInfo.PRECEDENCE_FIRST;    
	}    
	
	
	private SootMethod makeSootMethod(MethodSig method,int modifiers,SootClass cl) {
		Type retType = method.getReturnType().getSootType();
		List parms = new ArrayList();
		for( Iterator formalIt = method.getFormals().iterator(); formalIt.hasNext(); ) {
			final AbcType formalType = ((Formal) formalIt.next()).getType();
			parms.add(formalType.getSootType());
		}
		SootMethod sm = new SootMethod( method.getName(),  
											parms,
											retType,
											modifiers );
		for( Iterator exceptionIt = method.getSootExceptions().iterator(); exceptionIt.hasNext(); ) {
				final SootClass exception = (SootClass) exceptionIt.next();
				sm.addException( exception );
		}
		cl.addMethod(sm);
		return sm;
	}
	
	private SootMethod getMethod(MethodSig getSig, SootFieldRef fieldref,SootClass cl) {
		SootMethod sm = makeSootMethod(getSig,Modifier.PUBLIC,cl);
		//		create a body
		Body b = Jimple.v().newBody(sm); sm.setActiveBody(b);
		Chain ls = b.getLocals();
		PatchingChain ss = b.getUnits();
		//  target of the field reference is "this : targetType"
		SootClass sc = fieldref.declaringClass();
		RefType rt = sc.getType();
		ThisRef thisref = Jimple.v().newThisRef(rt);
		Local v = Jimple.v().newLocal("this$",rt); ls.add(v);
		IdentityStmt thisStmt = soot.jimple.Jimple.v().newIdentityStmt(v,thisref); ss.add(thisStmt);
		//  get the field we want to retrieve
		FieldRef sfref = Jimple.v().newInstanceFieldRef(b.getThisLocal(),fieldref);
		// 	return the value
		Local r = Jimple.v().newLocal("result$",fieldref.type());  ls.add(r);
		AssignStmt rStmt = soot.jimple.Jimple.v().newAssignStmt(r, sfref); ss.add(rStmt);
		ReturnStmt stmt = Jimple.v().newReturnStmt(r); 
		ss.add(stmt);
		// This is an accessor method for reading a field
		MethodCategory.registerFieldGet(fieldref,sm);
	    return sm;
	}
	
	private SootMethod setMethod(MethodSig getSig, SootFieldRef fieldref,SootClass cl) {
		SootMethod sm = makeSootMethod(getSig,Modifier.PUBLIC,cl);
		//		create a body
		Body b = Jimple.v().newBody(sm); sm.setActiveBody(b);
		Chain ls = b.getLocals();
		PatchingChain ss = b.getUnits();
		//  target of the field reference is "this : targetType"
		SootClass sc = fieldref.declaringClass();
		RefType rt = sc.getType();
		ThisRef thisref = Jimple.v().newThisRef(rt);
		Local v = Jimple.v().newLocal("this$",rt); ls.add(v);
		IdentityStmt thisStmt = soot.jimple.Jimple.v().newIdentityStmt(v,thisref); ss.add(thisStmt);
		//  get the field we want to update
		FieldRef sfref = Jimple.v().newInstanceFieldRef(b.getThisLocal(),fieldref);
		// 	get the parameter that we want to store
		Local p = Jimple.v().newLocal("param$",fieldref.type()); ; ls.add(p);
		ParameterRef pr = Jimple.v().newParameterRef(fieldref.type(),0);
		IdentityStmt prStmt = soot.jimple.Jimple.v().newIdentityStmt(p, pr); ss.add(prStmt);
		// now do the assignment
		AssignStmt rStmt = soot.jimple.Jimple.v().newAssignStmt(sfref, p); ss.add(rStmt);
		ReturnStmt stmt = Jimple.v().newReturnStmt(p); 
		ss.add(stmt);
		// This is an accessor method for writing a field
		MethodCategory.registerFieldSet(fieldref,sm);
		return sm;
	}
	

	private List fieldsToRemove = new ArrayList();
	
	public void removeFakeFields() {
		for (Iterator ftrit = fieldsToRemove.iterator(); ftrit.hasNext(); ) {
			SootField sf = (SootField) ftrit.next();
			// System.out.println("removing "+sf);
			sf.getDeclaringClass().removeField(sf);
		}
	}
	
    private void addField( IntertypeFieldDecl ifd ) {
        FieldSig field = ifd.getTarget();

        int modifiers = field.getModifiers();
        modifiers |= Modifier.PUBLIC;
        modifiers &= ~Modifier.PRIVATE;
        modifiers &= ~Modifier.PROTECTED;
        modifiers &= ~Modifier.FINAL; // FIXME: just to make the init stuff work:
                                      // initialisers are executed within the aspect
        
       
        SootClass cl = field.getDeclaringClass().getSootClass();
        
        if( cl.isInterface() ) {
        	// add the accessor methods to the interface
        	
        	// it is necessary to put a field into the interface, because in Soot
        	// it is not possible to give a field a declaring class without also
        	// putting it into that class.
        	SootField fakeField = new SootField(field.getName(),field.getType().getSootType(),field.getModifiers());
        	cl.addField(fakeField);
        	fieldsToRemove.add(fakeField);
        	MethodCategory.registerRealNameAndClass(fakeField,MethodCategory.getModifiers(field),
        					                        MethodCategory.getName(field),
        					                        AbcFactory.AbcClass(MethodCategory.getClass(field)));
			// System.out.println("added "+fakeField+" to "+cl);
        	
        	SootMethod getter = makeSootMethod(ifd.getGetter(),modifiers | Modifier.ABSTRACT,cl);
        	MethodCategory.registerFieldGet(fakeField,getter);
			SootMethod setter = makeSootMethod(ifd.getSetter(),modifiers | Modifier.ABSTRACT,cl);
			MethodCategory.registerFieldSet(fakeField,setter);
        
        	Set implementors = hierarchy.getAllImplementersOfInterface(cl);
            for( Iterator childClassIt = implementors.iterator(); childClassIt.hasNext(); ) {
                final SootClass childClass = (SootClass) childClassIt.next();
                if( childClass.isInterface() ) continue;
                if( childClass.hasSuperclass() 
                && implementors.contains(childClass.getSuperclass()) )
                    continue;
                    
                
	                // Add the field itself
	            SootField  newField = new SootField(
	                        field.getName(),
	                        field.getType().getSootType(),
	                        modifiers );
	            // childClass.addField(newField);
	            overrideITDfield(childClass,newField,ifd);
	   
                // System.out.println("adding field "+newField+ " with modifiers "  + Modifier.toString(modifiers) + " to class "+childClass);
                
                // Add the accessor methods and their implementation to the implementing class
                getMethod(ifd.getGetter(),newField.makeRef(),childClass);
                setMethod(ifd.getSetter(),newField.makeRef(),childClass);
                
                
                // System.out.println("added field "+field.getName() + " to class " + childClass);

            // TODO: add accessor methods

            }
        } else {
			
            // Add the field itself
            SootField newField = new SootField(
                    field.getName(),
                    field.getType().getSootType(),
                    modifiers );
            // cl.addField(newField);
            overrideITDfield(cl,newField,ifd);
        }

        // TODO: Add dispatch methods
    }
	
	private boolean overrideITDconstructor(SootClass pht, 
															 SootMethod mi) {
		boolean skipped = false;
		if (pht.declaresMethod(mi.getName(),mi.getParameterTypes())) {
			// System.out.println("it has the method already");
			SootMethod minst = pht.getMethod(mi.getName(),mi.getParameterTypes());
			if (zapsconstructor(mi,minst)){   
					// pht.removeMethod(minst); // FIXME: this must be wrong....
					pht.addMethod(mi);
			} else if (zapsconstructor(minst,mi)) {	
					skipped = true;
					}
				else { // pht.addMethod(mi); 
						  throw new InternalCompilerError("ITD conflicts with an existing class member");} 
			}
		else pht.addMethod(mi); 
		return !skipped;
	}
	
	
	boolean zapsconstructor(SootMethod mi1,SootMethod mi2) {
		if ((Modifier.isPrivate(mi2.getModifiers()) && !isIntertype(mi2)) && 
		     (!Modifier.isPrivate(mi1.getModifiers()) && isIntertype(mi1))) return true; // can always zap a private constructor
		if (!(isIntertype(mi1) && (isIntertype(mi2)))) return false;
		return abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getPrecedence(origin(mi1),origin(mi2)) == GlobalAspectInfo.PRECEDENCE_FIRST;    
	}
	
    
	private void addConstructor( IntertypeConstructorDecl icd) {
		SootClass sc = icd.getTarget().getSootClass();
		  if( sc.isInterface() ) {
		  		Set implementors = hierarchy.getAllImplementersOfInterface(sc);
				for( Iterator childClassIt = implementors.iterator(); childClassIt.hasNext(); ) {
					final SootClass childClass = (SootClass) childClassIt.next();
					if( childClass.isInterface() ) continue;
					if( childClass.hasSuperclass() 
					&& implementors.contains(childClass.getSuperclass()) )
						continue;

					createConstructor(childClass,icd);
                
					// System.out.println("added method "+ method.getName() + " to class " + childClass);
				}
			} else createConstructor(sc,icd);
	 }

	private void createConstructor( SootClass scTarget, IntertypeConstructorDecl icd ) {
    
		// constructors have void return type
		Type retType = soot.VoidType.v();
		
		// get the types of formal parameters
		List parms = new ArrayList();
		for( Iterator formalIt = icd.getFormalTypes().iterator(); formalIt.hasNext(); ) {
			final AbcType formalType = (AbcType) formalIt.next();
			parms.add(formalType.getSootType());
		}

		// set the modifiers
		int modifiers = icd.getModifiers();
		modifiers |= Modifier.PUBLIC;
		modifiers &= ~Modifier.PRIVATE;
		modifiers &= ~Modifier.PROTECTED;
       
		// create the method
		SootMethod sm = new SootMethod( 
					  "<init>",   // the name of a constructor is always "<init>"
					  parms,
					  retType,
					  modifiers );

		for( Iterator exceptionIt = icd.getExceptions().iterator(); exceptionIt.hasNext(); ) {
			final SootClass exception = (SootClass) exceptionIt.next();
			sm.addException( exception );
		}
		
		intertype.put(sm,icd.getAspect());
		// inject the new constructor into the target type
		// scTarget.addMethod(sm);	
		if (!overrideITDconstructor(scTarget,sm))
			return;
	    	
		// the body of the constructor is of the form
		//		qualifier.ccall(aspect.e1(f1,f2,...,fn), ..., aspect.ek(f1,f2,...,fn));  // super or this call
		//		aspect.body(this,f1,f2,...,fn)
		
		Body b = Jimple.v().newBody(sm); sm.setActiveBody(b);
		Chain ls = b.getLocals();
		PatchingChain ss = b.getUnits();
		// get the "this" reference
		RefType rt = scTarget.getType(); 
		ThisRef thisref = Jimple.v().newThisRef(rt); 
		Local thisLoc = Jimple.v().newLocal("this$loc",rt);  ls.add(thisLoc);
		IdentityStmt thisstmt = Jimple.v().newIdentityStmt(thisLoc,thisref); ss.add(thisstmt);
		// construct the arguments of the ei's and body: (f1,f2,...,fn)
		List eiArgs = new ArrayList();
		int index = 0;
		for (Iterator formals=parms.iterator(); formals.hasNext(); ) {
				 final Type formalType = (Type) formals.next();
				 Local p = Jimple.v().newLocal("$param"+index,formalType); ; ls.add(p);
				 ParameterRef pr = Jimple.v().newParameterRef(formalType,index);
				 IdentityStmt prStmt = soot.jimple.Jimple.v().newIdentityStmt(p, pr); ss.add(prStmt);
				 eiArgs.add(p);
				 index++;
		}
		// now set up the arguments for the ccall itself, by calling each of the ei
		// and storing the result in a local. Note that some arguments can just be references
		// to parameters, in which case there is no method to invoke.
		List ccArgs = new ArrayList(); index = 0;
		List ccArgsTypes = new ArrayList();
		for (Iterator args = icd.getArguments().iterator(); args.hasNext(); ) {
			Object arg = args.next();
			if (arg instanceof Integer) {
					int i = ((Integer) arg).intValue();
					// System.out.println("i="+i);
					ccArgs.add(eiArgs.get(i));
					ccArgsTypes.add(parms.get(i));
			}
			if (arg instanceof MethodSig) {
				// get the method that needs to be called
				SootMethodRef sa = ((MethodSig) arg).getSootMethodRef()	;			
				// now invoke the implementation in the originating aspect
				InvokeExpr ie = Jimple.v().newStaticInvokeExpr(sa,eiArgs);
				Local p = Jimple.v().newLocal("e"+index, sa.returnType());  ls.add(p);
				AssignStmt rStmt = soot.jimple.Jimple.v().newAssignStmt(p, ie); ss.add(rStmt);
				ccArgs.add(p); ccArgsTypes.add(sa.returnType());
			}
			index++;
		}
		// do the constructor call
		SootClass ccReceiver;
		if (icd.getQualifier() != null) // to an enclosing instance?
			ccReceiver = icd.getQualifier().getSootClass();
		else
			ccReceiver = scTarget;
		if (icd.getKind() == IntertypeConstructorDecl.SUPER) // go one up if it's super(..)
			ccReceiver = ccReceiver.getSuperclass();
		SootMethodRef constructor = Scene.v().makeConstructorRef(ccReceiver,ccArgsTypes);
	
		
		Expr ccallExpr = Jimple.v().newSpecialInvokeExpr(thisLoc,constructor,ccArgs);
		InvokeStmt ccall = Jimple.v().newInvokeStmt(ccallExpr);
		ss.add(ccall);
		
		// call static method aspect.body(this,f1,f2,...,fn)
		//  the first argument is "this"
		List bodyArgs = new ArrayList(eiArgs);
		bodyArgs.add(0,thisLoc);
		// System.out.println("getting method "+icd.getBody());
		SootMethod bodyMethod = icd.getBody().getSootMethod();
		Expr bodyExpr = Jimple.v().newStaticInvokeExpr(bodyMethod.makeRef(),bodyArgs);
		InvokeStmt body = Jimple.v().newInvokeStmt(bodyExpr);
		ss.add(body);
		
		//	return
		ReturnVoidStmt ret = Jimple.v().newReturnVoidStmt();
		ss.add(ret);
		
		
		// This is a stub for an intertype constructor decl
		MethodCategory.register(sm, MethodCategory.INTERTYPE_CONSTRUCTOR_DELEGATOR);
		MethodCategory.registerRealNameAndClass(sm, icd.getOriginalModifiers(), "<init>", AbcFactory.AbcClass(scTarget),
							0,icd.hasMangleParam() ? 1 : 0);
	}
	

   
    
    /** weaving of intertype field initialisations.
     *
     * initialisers for intertype fields are woven into each super-calling constructor of the target class,
     * immediately after the super call.
     * 
     * The order in which the initialisations occur is determined as follows: 
     * 
     * - multiple initialisations from the same aspect onto the same class occur in 
     *   the same textual order as in the aspect
     * 
     * - when there are multiple aspects that introduce initialisers onto the same class, the order is determined
     *   by precedence: if A precedes B, then A:(C.foo) is initialised before B:(C.bar) 
     *   An error should be flagged if A and B are in the same precedence cycle.
     * 
     * - in cases where the field arrived via an interface, the order is determined by the order of
     *   the interface initialisation joinpoints, as defined below.
     * 
     *   Every super-calling constructor contains, immediately after the super call, a
	 *   sequence of interface initialization joinpoints. The list of interfaces in this
     *   sequence can be constructed as follows:
     * 
     *   Call the class of the constructor C, and call the superclass of C D.
     * 
     *      For each interface I directly implemented by C in textual order
     *         process(I)
     *
     *   where process(I) is
     *      if D does not (directly or indirectly) implement I and I is not in the list
     *       for each direct superinterface J of I in textual order
     *          process(J)
     *       add I to the list
     *
     */
    
       
    private List getInterfaceInits(SootClass c) {
		List initJoinPoints = new LinkedList();
		SootClass object = Scene.v().getSootClass("java.lang.Object");
		if (object.equals(c))
			return initJoinPoints;
		SootClass d = c.getSuperclass();
		for (Iterator itfs = c.getInterfaces().iterator(); itfs.hasNext(); ) {
			SootClass i = (SootClass) itfs.next();
			process(d,i,initJoinPoints);
		}
		return initJoinPoints;
    }
    
    
    private void process(SootClass d, SootClass i, List list) {
    	// not entirely sure about the first conjunct
    	if (!hierarchy.canStoreType(d.getType(),i.getType()) && !list.contains(i)) {
    		for (Iterator itfs = i.getInterfaces().iterator(); itfs.hasNext(); ) {
    			SootClass j = (SootClass) itfs.next();
    			process(d,j,list);
    		}
    		list.add(0,i);
    	}
    }
    
    
    private boolean precedes(IntertypeFieldDecl ifd1, IntertypeFieldDecl ifd2) {
    	if (ifd1.getInit() == null || ifd2.getInit()== null) return false;
    	if (ifd1.getAspect() == ifd2.getAspect())
    		return ifd1.getPosition().line() < ifd2.getPosition().line();
    	else { int cmp = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getPrecedence(ifd1.getAspect(),ifd2.getAspect()) ;
    		/*// this appears to generate nondeterministic behaviour, due
    		   // to iteration over a set or map. commented out for now...
    			if (cmp == GlobalAspectInfo.PRECEDENCE_NONE)
			       abc.main.Main.v().error_queue.enqueue(new polyglot.util.ErrorInfo(
														   polyglot.util.ErrorInfo.WARNING,
														   "Unspecified order with intertype initialiser at "+ ifd1.getPosition() +
														   "(consider using a precedence declaration).",
														   ifd2.getPosition())); */
    		   return (cmp == GlobalAspectInfo.PRECEDENCE_FIRST);
    	}
    }
    
    
    private IntertypeFieldDecl findNoPrec(List ifds) {
    	for (Iterator ifdsIt = ifds.iterator(); ifdsIt.hasNext(); ) {
    		IntertypeFieldDecl cand = (IntertypeFieldDecl) ifdsIt.next();
    		boolean noprec = true;
    		for (Iterator ifdsIt2 = ifds.iterator(); ifdsIt2.hasNext() && noprec; ) {
    			IntertypeFieldDecl cmp = (IntertypeFieldDecl) ifdsIt2.next();
    			noprec = !precedes(cmp,cand);
    		}
    		if (noprec) return cand;
    	}
    	IntertypeFieldDecl cand1 = (IntertypeFieldDecl) ifds.get(0);
    	IntertypeFieldDecl cand2 = (IntertypeFieldDecl) ifds.get(1);
    	abc.main.Main.v().error_queue.enqueue(new polyglot.util.ErrorInfo(
	    	                                           polyglot.util.ErrorInfo.SEMANTIC_ERROR,
	    	                                           "Precedence conflict with intertype initialiser at "+cand1.getPosition()+".",
	    	                                           cand2.getPosition()));
    	return (IntertypeFieldDecl) ifds.get(0);
    }
    
    private List sortWithPrec(List ifds) {
    	List result = new LinkedList();
    	List work = new LinkedList(ifds);
    	while (!work.isEmpty()) {
    		IntertypeFieldDecl ifd = findNoPrec(work);
    		work.remove(ifd);
    		result.add(0,ifd); // reverse order!
    	}
    	return result;
    }
    
	private class InterfaceInits {
		SootClass intrface;
		List/*<IntertypeFieldDecl>*/ ifds = new LinkedList(); 
		InterfaceInits(SootClass itf) {
			intrface = itf;
		}
	}
	
	private void sortWithPrec(InterfaceInits ifis) {
		ifis.ifds = sortWithPrec(ifis.ifds);
	}
	
	private class ITDInits {
		List/*<InterfaceInits>*/ interfaceInits = new LinkedList();
		List/*<IntertypeFieldDecl>*/ instanceInits = new LinkedList();
		List/*<IntertypeFieldDecl>*/ staticInits = new LinkedList();
		private void add(IntertypeFieldDecl ifd) {
			if (Modifier.isStatic(ifd.getTarget().getModifiers())) {
				staticInits.add(ifd); return;
			}
			SootClass targetClass = ifd.getTarget().getDeclaringClass().getSootClass();
			if (Modifier.isInterface(ifd.getTarget().getDeclaringClass().getSootClass().getModifiers())) {
				for (Iterator itfi = interfaceInits.iterator(); itfi.hasNext(); ) {
					InterfaceInits itf = (InterfaceInits) itfi.next();
					if (itf.intrface.equals(targetClass)) {
						itf.ifds.add(ifd);
						return;
					}
				}
				throw new InternalCompilerError("Interface init without an initialisation joinpoint");
			}
			instanceInits.add(ifd);
		}
		
	}
	
	private void sortWithPrec(ITDInits itdinits) {
		for (Iterator itfis = itdinits.interfaceInits.iterator(); itfis.hasNext(); ) {
			InterfaceInits ifi = (InterfaceInits) itfis.next();
			sortWithPrec(ifi);
		}
		itdinits.instanceInits = sortWithPrec(itdinits.instanceInits);
		itdinits.staticInits = sortWithPrec(itdinits.staticInits);
	}
	
	
	private Set getITDFieldsOfclass(Map classToITDfields, SootClass cl) {
		if (classToITDfields.containsKey(cl)) {
			return (Set) classToITDfields.get(cl);
		} else return new HashSet();
	}
	
	private Map invertFieldITTargetsMap() {
		Map result = new HashMap();
		for (Iterator entries = fieldITtargets.entrySet().iterator(); entries.hasNext(); ) {
			Map.Entry e = (Map.Entry) entries.next();
			IntertypeFieldDecl ifd = (IntertypeFieldDecl) e.getKey();
			Set classes = (Set) e.getValue();
			for (Iterator cls = classes.iterator(); cls.hasNext(); ) {
				SootClass cl = (SootClass) cls.next();
				Set ifds;
				if (result.containsKey(cl))
					ifds = (Set) result.get(cl);
				else
					ifds = new HashSet();
				ifds.add(ifd);
				result.put(cl,ifds);
			}
		}
		return result;
	}
	
	public void initialisers() {
		Map classToITDfields = invertFieldITTargetsMap();
		for (Iterator wit = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getWeavableClasses().iterator(); wit.hasNext(); ) {
			SootClass cl = ((AbcClass) wit.next()).getSootClass();
			ITDInits itdins = new ITDInits();
			List initItfs = getInterfaceInits(cl);
			for (Iterator iitfs = initItfs.iterator(); iitfs.hasNext(); ) {
				SootClass iitf = (SootClass) iitfs.next();
				itdins.interfaceInits.add(new InterfaceInits(iitf));
			}
			for (Iterator cls = getITDFieldsOfclass(classToITDfields,cl).iterator(); cls.hasNext(); ) {
				IntertypeFieldDecl ifd = (IntertypeFieldDecl) cls.next();
				itdins.add(ifd);
			}
			sortWithPrec(itdins);
			initialiseFields(cl,itdins);
		}
	}
	
	
    public static class ITDInitEndNopTag implements Tag {
	public final static String name="ITDInitEndNopTag";
	
	public String getName() {
	    return name;
	}
	
	public byte[] getValue() {
	    throw new AttributeValueException();
	}

	public String toString() {
	    return "End of ITD field inits";
	}
    }


	public void initialiseFields(SootClass cl, ITDInits itdins) {
		for (Iterator ifds = itdins.staticInits.iterator(); ifds.hasNext(); ) {
			IntertypeFieldDecl ifd = (IntertypeFieldDecl) ifds.next();
			initialiseStaticField(cl,ifd);
		}
		weaveInitNopWithTag(new ITDInitEndNopTag(),cl);
		for (Iterator ifds = itdins.instanceInits.iterator(); ifds.hasNext(); ) {
			IntertypeFieldDecl ifd = (IntertypeFieldDecl) ifds.next();
			initialiseInstanceField(cl,ifd);
		}
		for (Iterator ifisIt = itdins.interfaceInits.iterator(); ifisIt.hasNext(); ) {
			InterfaceInits ifis = (InterfaceInits) ifisIt.next();
			initialiseInterfaceFields(cl,ifis);
		}
	}
	
	
    
	private void initialiseInstanceField( SootClass cl, IntertypeFieldDecl ifd ) {
		if (ifd.getInit() == null)
			return;
		FieldSig field = ifd.getTarget();

		int modifiers = field.getModifiers();
		modifiers |= Modifier.PUBLIC;
		modifiers &= ~Modifier.PRIVATE;
		modifiers &= ~Modifier.PROTECTED;

		weaveInit(ifd,cl.getField(field.getName(),field.getType().getSootType()),modifiers, cl);
	}

    public static class InterfaceInitNopTag implements Tag {
	public final static String name="InterfaceInitNopTag";

	public boolean isStart;
	public SootClass intrface;
	
	public InterfaceInitNopTag(SootClass intrface,boolean isStart) {
	    this.intrface=intrface;
	    this.isStart=isStart;
	}
	
	public String getName() {
	    return name;
	}
	
	public byte[] getValue() {
	    throw new AttributeValueException();
	}

	public String toString() {
	    return (isStart ? "Start" : "End")+" interface initialization: "+intrface;
	}
    }

	
	private void initialiseInterfaceFields( SootClass cl, InterfaceInits ifis ) {
   	    Tag endtag=new InterfaceInitNopTag(ifis.intrface,false);
	    weaveInitNopWithTag(endtag,cl);
	    for (Iterator ifds = ifis.ifds.iterator(); ifds.hasNext() ; ) {
		IntertypeFieldDecl ifd = (IntertypeFieldDecl) ifds.next();
		initialiseInstanceField(cl,ifd);
	    }
   	    Tag starttag=new InterfaceInitNopTag(ifis.intrface,true);
	    weaveInitNopWithTag(starttag,cl);
	}
	
	private void initialiseStaticField( SootClass cl, IntertypeFieldDecl ifd ) {
		if (ifd.getInit() == null)
			return;
		FieldSig field = ifd.getTarget();

		int modifiers = field.getModifiers();
		modifiers |= Modifier.PUBLIC;
		modifiers &= ~Modifier.PRIVATE;
		modifiers &= ~Modifier.PROTECTED;

		weaveStaticInit(ifd,cl.getField(field.getName(),field.getType().getSootType()),modifiers, cl);
	}
	
	private SootMethod addStaticInitToAspect(IntertypeFieldDecl ifd, SootField sf, int modifiers, SootClass cl) {
		Type retType = VoidType.v();
		List parms = new ArrayList(); // no parameters
		String name = UniqueID.newID("fieldinit");
		SootMethod sm = new SootMethod( name,  
										parms,
										retType,
										Modifier.STATIC | Modifier.PUBLIC );
		// get the method that initialises this field
		// which is a static method of the aspect that contains the ITD
		SootMethod initialiser = ifd.getInit().getSootMethod();
		for( Iterator exceptionIt = ifd.getInit().getSootExceptions().iterator(); exceptionIt.hasNext(); ) {
			final SootClass exception = (SootClass) exceptionIt.next();
			sm.addException( exception );
		}
		ifd.getAspect().getInstanceClass().getSootClass().addMethod(sm);
		MethodCategory.register(sm,MethodCategory.INTERTYPE_INITIALIZER_DELEGATE);
		MethodCategory.registerRealNameAndClass(sm, sm.getModifiers(), name, AbcFactory.AbcClass(cl), 0,0);
		Body b = Jimple.v().newBody(sm); sm.setActiveBody(b);
		Chain ls = b.getLocals();
		PatchingChain ss = b.getUnits();
		// create the call
		List args = new ArrayList();
		InvokeExpr ie = Jimple.v().newStaticInvokeExpr(initialiser.makeRef(),args);
		Local res = Jimple.v().newLocal("result",initialiser.getReturnType()); ls.add(res);
		AssignStmt as = Jimple.v().newAssignStmt(res,ie);  ss.add(as);
		// get the field we want to initialise
		FieldRef sfref = Jimple.v().newStaticFieldRef(sf.makeRef()); 
		//  assign the value
		AssignStmt rStmt = soot.jimple.Jimple.v().newAssignStmt(sfref, res);  ss.add(rStmt);
		// return
		ReturnVoidStmt ret = soot.jimple.Jimple.v().newReturnVoidStmt(); ss.add(ret);
		return sm;
	}
	private SootMethod addInstInitToAspect(
			IntertypeFieldDecl ifd,
		SootField sf, int modifiers, SootClass cl) {
		Type retType = VoidType.v();
		// one parameter, namely "this" of target
		List parms = new ArrayList(); 
		parms.add(cl.getType());
		String name = UniqueID.newID("fieldinit");
		SootMethod sm = new SootMethod( name,  
										parms,
										retType,
										Modifier.STATIC | Modifier.PUBLIC );
		// get the method that initialises this field
		// which is a static method of the aspect that contains the ITD
		SootMethod initialiser = ifd.getInit().getSootMethod();
		for( Iterator exceptionIt = ifd.getInit().getSootExceptions().iterator(); exceptionIt.hasNext(); ) {
			final SootClass exception = (SootClass) exceptionIt.next();
			sm.addException( exception );
		}
		ifd.getAspect().getInstanceClass().getSootClass().addMethod(sm);
		MethodCategory.register(sm,MethodCategory.INTERTYPE_INITIALIZER_DELEGATE);
		MethodCategory.registerRealNameAndClass(sm, sm.getModifiers(), name, 
		                          AbcFactory.AbcClass(cl), 1,0);
		Body b = Jimple.v().newBody(sm); sm.setActiveBody(b);
		Chain ls = b.getLocals();
		PatchingChain ss = b.getUnits();
		// 	the method that initialises this field
		//	which is a static method of the aspect that contains the ITD
		SootMethod smInit = ifd.getInit().getSootMethod(); 
		//	now create the call
		List args = new ArrayList();
		ParameterRef thisPref = Jimple.v().newParameterRef(cl.getType(),0);
		Local v = Jimple.v().newLocal("thisparam",cl.getType()); ls.add(v);
		IdentityStmt thisStmt = soot.jimple.Jimple.v().newIdentityStmt(v,thisPref); ss.add(thisStmt);
		args.add(v); // the only argument is "this"
		InvokeExpr ie = Jimple.v().newStaticInvokeExpr(smInit.makeRef(),args); 
		Local res = Jimple.v().newLocal("result",smInit.getReturnType()); ls.add(res);
		AssignStmt as = Jimple.v().newAssignStmt(res,ie); ss.add(as);
		if (ifd.getSetter() == null) {
			//  get the field we want to initialise
				FieldRef sfref = Jimple.v().newInstanceFieldRef(v,sf.makeRef());
			//  assign the value
				AssignStmt rStmt = soot.jimple.Jimple.v().newAssignStmt(sfref, res); 
				ss.add(rStmt);
		} else {
			List setargs = new ArrayList();
			setargs.add(res);
			InvokeExpr sie;
			if (ifd.getSetter().getDeclaringClass().getSootClass().isInterface()) {
					sie = Jimple.v().newInterfaceInvokeExpr(v,
								   ifd.getSetter().getSootMethod().makeRef(),setargs);
				} else {
					sie = Jimple.v().newVirtualInvokeExpr(v,
									ifd.getSetter().getSootMethod().makeRef(),setargs); }
				InvokeStmt istmt = Jimple.v().newInvokeStmt(sie);
				ss.add(istmt);
		}
		ReturnVoidStmt ret = Jimple.v().newReturnVoidStmt(); ss.add(ret);
		return sm;
	}
	
    private void weaveInitNopWithTag(Tag tag,SootClass cl) {
	SootClass object = Scene.v().getSootClass("java.lang.Object");
	if (object.equals(cl))
			return; // Object doesn't have an init to weave into
	for (Iterator ms = cl.methodIterator(); ms.hasNext(); ) {
	    SootMethod clsm = (SootMethod) ms.next();
	    if (clsm.getName().equals(SootMethod.constructorName)) {
		Body b = clsm.getActiveBody();
		Chain ss = b.getUnits();
		// find unique super.<init>, throw exception if it's not unique
		Stmt initstmt = abc.soot.util.Restructure.findInitStmt(ss);
		// the following needs to be inserted right after initstmt
		if (initstmt.getInvokeExpr().getMethodRef().declaringClass() == cl.getSuperclass()) {
		    Chain units = b.getUnits();
		    Stmt followingstmt = (Stmt) units.getSuccOf(initstmt);
		    // create a call to a method that calls the appropriate init from the aspect.
		    // this method is necessary to handle "within" correctly: the init happens
		    // lexically within the aspect
		    Stmt nopstmt=Jimple.v().newNopStmt();
		    nopstmt.addTag(tag);
		    units.insertBefore(nopstmt,followingstmt);
		}
	    }
	}	
    }

	
	private void weaveInit(
		IntertypeFieldDecl ifd,
		SootField sf,
		int modifiers,
		SootClass cl) {
		// add a call to the initialiser method to any constructor of target class
		// that calls "super.<init>"
		for (Iterator ms = cl.methodIterator(); ms.hasNext(); ) {
			SootMethod clsm = (SootMethod) ms.next();
			if (clsm.getName().equals(SootMethod.constructorName)) {
				Body b = clsm.getActiveBody();
				Chain ss = b.getUnits();
				// find unique super.<init>, throw exception if it's not unique
				Stmt initstmt = abc.soot.util.Restructure.findInitStmt(ss);
				// the following needs to be inserted right after initstmt
				if (initstmt.getInvokeExpr().getMethodRef().declaringClass() == cl.getSuperclass()) {
					Chain units = b.getUnits();
					Stmt followingstmt = (Stmt) units.getSuccOf(initstmt);
					// create a call to a method that calls the appropriate init from the aspect.
					// this method is necessary to handle "within" correctly: the init happens
					// lexically within the aspect
				    SootMethod initdel = addInstInitToAspect(ifd, sf, modifiers, cl);
				    List args = new ArrayList();
					args.add(b.getThisLocal());
				    InvokeExpr ie = Jimple.v().newStaticInvokeExpr
					(initdel.makeRef(),args);
				    InvokeStmt is = Jimple.v().newInvokeStmt(ie);
				    units.insertBefore(is,followingstmt);
				}
			}
		}
	}

	private void weaveStaticInit(
		IntertypeFieldDecl ifd,
		SootField sf,
		int modifiers,
		SootClass cl) {
		// add a call to the initialiser method from clinit of target class
		// if it doesn't exist, create one.	
			SootMethod clinit; Body b;
			try { 
			    clinit = cl.getMethod(SootMethod.staticInitializerName,
						   new ArrayList());
				b = clinit.getActiveBody();
			} catch (java.lang.RuntimeException s) {
				clinit = new SootMethod( 
							SootMethod.staticInitializerName,   
							new ArrayList(),
							VoidType.v(),
							Modifier.STATIC | Modifier.PUBLIC );
				b = Jimple.v().newBody(clinit); clinit.setActiveBody(b);
				cl.addMethod(clinit);
				Chain ss = b.getUnits();
				ReturnVoidStmt ret = Jimple.v().newReturnVoidStmt();
				ss.add(ret);
			}
			Chain ss = b.getUnits();
		// find first normal statement
			Stmt initstmt = abc.soot.util.Restructure.findFirstRealStmt(clinit,ss);
		// create a call to a method that calls the appropriate init from the aspect.
		// this method is necessary to handle "within" correctly: the init happens
		// lexically within the aspect
			SootMethod initdel = addStaticInitToAspect(ifd,sf,modifiers,cl);
			List args = new ArrayList();
			InvokeExpr ie = Jimple.v().newStaticInvokeExpr(initdel.makeRef(),args);
			InvokeStmt is = Jimple.v().newInvokeStmt(ie);
			ss.insertBefore(is,initstmt);
	}

	

}
