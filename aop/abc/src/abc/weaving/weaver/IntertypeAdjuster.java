package abc.weaving.weaver;
import soot.*;
import soot.util.*;
import soot.jimple.*;
import java.util.*;

import polyglot.util.UniqueID;
import polyglot.util.InternalCompilerError;

import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;

public class IntertypeAdjuster {
	
	public void adjust() {
    // Generate Soot signatures for intertype methods and fields
 
    //  generate accessors for qualifier.this
    	for( Iterator qtsIt = GlobalAspectInfo.v().getQualThiss().iterator(); qtsIt.hasNext(); ) {
    		final QualThis qts = (QualThis) qtsIt.next();
    		addQualThis( qts );
    	} 
    // 	weave in intertype methods
        for( Iterator imdIt = GlobalAspectInfo.v().getIntertypeMethodDecls().iterator(); imdIt.hasNext(); ) {
            final IntertypeMethodDecl imd = (IntertypeMethodDecl) imdIt.next();
            addMethod( imd );
        }
   // 	weave in intertype constructors
		for( Iterator imdIt = GlobalAspectInfo.v().getIntertypeConstructorDecls().iterator(); imdIt.hasNext(); ) {
		 	final IntertypeConstructorDecl icd = (IntertypeConstructorDecl) imdIt.next();
		 	addConstructor( icd );
	 	} 
   	//	weave in intertype fields
        for( Iterator ifdIt = GlobalAspectInfo.v().getIntertypeFieldDecls().iterator(); ifdIt.hasNext(); ) {
            final IntertypeFieldDecl ifd = (IntertypeFieldDecl) ifdIt.next();
            addField( ifd );
        }
	// 	generate accessors for getters super.field
		for ( Iterator spfdIt = GlobalAspectInfo.v().getSuperFieldGetters().iterator(); spfdIt.hasNext(); ) {
			final SuperFieldGet sfd = (SuperFieldGet) spfdIt.next();
			addSuperFieldGetter( sfd );
		}
	 // 	generate accessors for getters super.field
		 for ( Iterator spfdIt = GlobalAspectInfo.v().getSuperFieldSetters().iterator(); spfdIt.hasNext(); ) {
			 final SuperFieldSet sfd = (SuperFieldSet) spfdIt.next();
				 addSuperFieldSetter( sfd );
		 }
	 // 	generate accessors for super.call
		 for( Iterator spdIt = GlobalAspectInfo.v().getSuperDispatches().iterator(); spdIt.hasNext(); ) {
			 final SuperDispatch sd = (SuperDispatch) spdIt.next();
			 addSuperDispatch( sd );
		 }       
    }
    
	private void addQualThis( QualThis qts ) {
	   // the signature of the method that we should generate
	   		MethodSig method = qts.getMethod();  		
	   // 	create a new method for the dispatch
		    Type retType = method.getReturnType().getSootType();
		    List parms = new ArrayList(); // no parameters
		   int modifiers = Modifier.PUBLIC;	
	   // 	create the method
		  	SootMethod sm = new SootMethod( 
									 method.getName(),   // the new name in the target
									 parms,
									 retType,
									 modifiers );
		// 	add method to the target class
			SootClass sc = qts.getTarget().getSootClass();
			sc.addMethod(sm);
	   //	create a body
		   	Body b = Jimple.v().newBody(sm); sm.setActiveBody(b);
		   	Chain ls = b.getLocals();
		   	PatchingChain ss = b.getUnits();
		//  make sure we have a thislocal
			ThisRef tr = Jimple.v().newThisRef(sc.getType());
			Local thisloc = Jimple.v().newLocal("thisloc",sc.getType()); ls.add(thisloc);
			IdentityStmt ids = Jimple.v().newIdentityStmt(thisloc,tr); ss.add(ids);
	   //   do the real work...   	
		   	Local v = getThis(b, qts.getQualifier().getSootClass().getType());   
	   //   and return the value...
	   		ReturnStmt ret = Jimple.v().newReturnStmt(v); ss.add(ret);
	   
	   //  	This is an accessor method for getting this
	   //   it should be invisible to advice weaving
	   		MethodCategory.register(sm, MethodCategory.THIS_GET);
	   }
	   
	   /** return a local that contains "qualifier.this", adding the relevant statements to the body. 
	    *  This code is adapted from JavaToJimple.JimpleBodyBuilder.
	    */ 
	   private Local getThis(Body b, Type sootType){
			// if need this just return it
			if (b.getThisLocal().getType().equals(sootType)) 
				return b.getThisLocal();
        
			// otherwise get this$0 for one level up
			SootClass classToInvoke = ((soot.RefType)b.getThisLocal().getType()).getSootClass();
			SootField outerThisField = classToInvoke.getFieldByName("this$0");
			Local t1 = Jimple.v().newLocal("this$0$loc",outerThisField.getType());
			b.getLocals().add(t1);
			        
			FieldRef fieldRef = soot.jimple.Jimple.v().newInstanceFieldRef(b.getThisLocal(), outerThisField);
			AssignStmt fieldAssignStmt = soot.jimple.Jimple.v().newAssignStmt(t1, fieldRef);
			b.getUnits().add(fieldAssignStmt);
              
			// otherwise make a new access method
			soot.Local t2 = t1;
			while (!t2.getType().equals(sootType)){
				//System.out.println("t2 type: "+t2.getType());
				classToInvoke = ((soot.RefType)t2.getType()).getSootClass();
				// make an access method and add it to that class for accessing 
				// its private this$0 field
				SootMethod methToInvoke = makeOuterThisAccessMethod(classToInvoke);
				// invoke that method
				Local t3 = Jimple.v().newLocal("invoke$loc",methToInvoke.getReturnType()); b.getLocals().add(t3);
				InvokeExpr ie = Jimple.v().newVirtualInvokeExpr(t2,methToInvoke);
				AssignStmt rStmt = soot.jimple.Jimple.v().newAssignStmt(t3, ie); b.getUnits().add(rStmt);
				// ready for next iteration		
				t2 = t3;
			}
			return t2;        
		}
    
		private soot.SootMethod makeOuterThisAccessMethod(soot.SootClass classToInvoke){
			// create the method
			String name = UniqueID.newID("access$this$0$");
			ArrayList paramTypes = new ArrayList();
			SootMethod meth = new SootMethod(name, paramTypes, classToInvoke.getFieldByName("this$0").getType(), soot.Modifier.PUBLIC);
			//	add to target class
			classToInvoke.addMethod(meth);
			// now fill in the body
			Body b = Jimple.v().newBody(meth); meth.setActiveBody(b);
			Chain ss = b.getUnits(); Chain ls = b.getLocals();
			// generate local for "this"
			SootField sf = classToInvoke.getFieldByName("this$0");
			ThisRef thiz = Jimple.v().newThisRef(classToInvoke.getType());
			Local thizloc = Jimple.v().newLocal("this$loc",classToInvoke.getType()); ls.add(thizloc);
			IdentityStmt ids = Jimple.v().newIdentityStmt(thizloc,thiz); ss.add(ids);
			// assign res = this.this$0
			FieldRef fr = Jimple.v().newInstanceFieldRef(thizloc,classToInvoke.getFieldByName("this$0")); 
			Local res = Jimple.v().newLocal("result",sf.getType()); ls.add(res);
			AssignStmt astmt = Jimple.v().newAssignStmt(res,fr); ss.add(astmt);
			// return res
			ReturnStmt ret = Jimple.v().newReturnStmt(res); ss.add(ret);	
			
			return meth;
		}
    
    private void addSuperFieldGetter( SuperFieldGet sfd ) {
    // 	the field that we wish to access, in the superclass of sd.target()
    	FieldSig field = sfd.getFieldSig();
    	
    // 	create a new method for the dispatch
    	Type retType = field.getType().getSootType();
    	List parms = new ArrayList(); // no parameters
    	int modifiers = Modifier.PUBLIC;
    	
	// 	create the method
		SootMethod sm = new SootMethod( 
								  sfd.getName(),   // the new name in the target
								  parms,
								  retType,
								  modifiers );
	//	create a body
		Body b = Jimple.v().newBody(sm); sm.setActiveBody(b);
		Chain ls = b.getLocals();
		PatchingChain ss = b.getUnits();
	//  target of the field reference is "this : targetType"
		SootClass sc = sfd.getTarget().getSootClass();
		RefType rt = sc.getType();
		ThisRef thisref = Jimple.v().newThisRef(rt);
		Local v = Jimple.v().newLocal("this$",rt); ls.add(v);
		IdentityStmt thisStmt = soot.jimple.Jimple.v().newIdentityStmt(v,thisref); ss.add(thisStmt);
	//  get the field we want to retrieve
		SootField sf = field.getSootField();
		FieldRef sfref = Jimple.v().newInstanceFieldRef(b.getThisLocal(),sf);
	// 	return the value
		Local r = Jimple.v().newLocal("result$",retType);  ls.add(r);
		AssignStmt rStmt = soot.jimple.Jimple.v().newAssignStmt(r, sfref); ss.add(rStmt);
		ReturnStmt stmt = Jimple.v().newReturnStmt(r); 
		ss.add(stmt);
	// 	add method to the target class
		sfd.getTarget().getSootClass().addMethod(sm);				  


		// This is an accessor method for reading a field
		MethodCategory.register(sm, MethodCategory.ACCESSOR_GET);
		MethodCategory.registerRealNameAndClass(sm, field.getName(), field.getDeclaringClass().getJavaName(),
							0,0);
    }
    
	private void addSuperFieldSetter( SuperFieldSet sfd ) {
		//	System.out.println("adding super field set" + sfd.getName() + " to "+sfd.getTarget());
		// 	the field that we wish to access, in the superclass of sd.target()
			FieldSig field = sfd.getFieldSig();
    	
		// 	create a new method for the dispatch
			Type retType = field.getType().getSootType();
			List parms = new ArrayList(); // no parameters
			parms.add(retType);
			int modifiers = Modifier.PUBLIC;
    	
		// 	create the method
			SootMethod sm = new SootMethod( 
									  sfd.getName(),   // the new name in the target
									  parms,
									  retType,
									  modifiers );
		//	create a body
			Body b = Jimple.v().newBody(sm); sm.setActiveBody(b);
			Chain ls = b.getLocals();
			PatchingChain ss = b.getUnits();
		//  target of the field reference is "this : targetType"
			SootClass sc = sfd.getTarget().getSootClass();
			RefType rt = sc.getType();
			ThisRef thisref = Jimple.v().newThisRef(rt);
			Local v = Jimple.v().newLocal("this$",rt); ls.add(v);
			IdentityStmt thisStmt = soot.jimple.Jimple.v().newIdentityStmt(v,thisref); ss.add(thisStmt);
		//  get the field we want to retrieve
			SootField sf = field.getSootField();
			FieldRef sfref = Jimple.v().newInstanceFieldRef(b.getThisLocal(),sf);
		//	make a local for the value parameter
			Local p = Jimple.v().newLocal("param$",sf.getType()); ; ls.add(p);
			ParameterRef pr = Jimple.v().newParameterRef(sf.getType(),0);
			IdentityStmt prStmt = soot.jimple.Jimple.v().newIdentityStmt(p, pr); ss.add(prStmt);
		//  do the assignment
			AssignStmt rStmt = soot.jimple.Jimple.v().newAssignStmt(sfref, p); ss.add(rStmt);
		// 	return the value
			ReturnStmt stmt = Jimple.v().newReturnStmt(p); 
			ss.add(stmt);
		// 	add method to the target class
			sfd.getTarget().getSootClass().addMethod(sm);				  

		// This is an accessor method for reading a field
			MethodCategory.register(sm, MethodCategory.ACCESSOR_SET);
			MethodCategory.registerRealNameAndClass(sm, field.getName(), field.getDeclaringClass().getJavaName(),
								0,0);
		}
    

	private void addSuperDispatch( SuperDispatch sd ) {
	// the method that we wish to call, in the superclass of sd.target()
		MethodSig method = sd.getMethodSig(); 
		
	// create a new method for the dispatch
		Type retType = method.getReturnType().getSootType();
		List parms = new ArrayList();
		for( Iterator formalIt = method.getFormals().iterator(); formalIt.hasNext(); ) {
				final AbcType formalType = ((Formal) formalIt.next()).getType();
				parms.add(formalType.getSootType());
		}

		int modifiers = method.getModifiers();
		modifiers |= Modifier.PUBLIC;
		modifiers &= ~Modifier.PRIVATE;
		modifiers &= ~Modifier.PROTECTED;
            
		// Create the method
		SootMethod sm = new SootMethod( 
							  sd.getName(),   // the new name in the target
							  parms,
							  retType,
							  modifiers );

		for( Iterator exceptionIt = method.getExceptions().iterator(); exceptionIt.hasNext(); ) {
				final SootClass exception = (SootClass) exceptionIt.next();
				sm.addException( exception );
		}
		
		/* generate call to implementation: specialinvoke this.<Superclass: method> ( arg1, arg2, ..., argn) */	
			//create a body
				Body b = Jimple.v().newBody(sm); sm.setActiveBody(b);
				Chain ls = b.getLocals();
				PatchingChain ss = b.getUnits();
			// argument set-up
				List args = new LinkedList();
			//  create references to the  parameters
				int index = 0;
				for (Iterator formals=parms.iterator(); formals.hasNext(); ) {
					final Type formalType = (Type) formals.next();
					Local p = Jimple.v().newLocal("param$"+index,formalType); ; ls.add(p);
					ParameterRef pr = Jimple.v().newParameterRef(formalType,index);
					IdentityStmt prStmt = soot.jimple.Jimple.v().newIdentityStmt(p, pr); ss.add(prStmt);
					args.add(p);
					index++;
				}
			//	the target of the invoke is "this : TargetType"
				SootClass sc = sd.getTarget().getSootClass();
				RefType rt = sc.getType(); 
				ThisRef thisref = Jimple.v().newThisRef(rt); 
				Local v = Jimple.v().newLocal("this$loc",rt); ; ls.add(v);
				IdentityStmt thisStmt = soot.jimple.Jimple.v().newIdentityStmt(v, thisref); ss.add(thisStmt);
			// now invoke the method in super class
				SootMethod ssm = method.getSootMethod();
				InvokeExpr ie = Jimple.v().newSpecialInvokeExpr(v,ssm,args);
			// if this is a void returntype, create call followed by return
			// otherwise return the value directly
				if (retType.equals(VoidType.v())) {
					InvokeStmt stmt1 = Jimple.v().newInvokeStmt(ie);
					ReturnVoidStmt stmt2 = Jimple.v().newReturnVoidStmt();
					ss.add(stmt1); ss.add(stmt2);
				} else {
					Local r = Jimple.v().newLocal("result",retType);  ls.add(r);
					AssignStmt rStmt = soot.jimple.Jimple.v().newAssignStmt(r, ie); ss.add(rStmt);
					ReturnStmt stmt = Jimple.v().newReturnStmt(r); 
					ss.add(stmt);
				}
			// Add method to the target class
				sc.addMethod(sm);

				// This is an intertype special call delegator
				MethodCategory.register(sm, MethodCategory.INTERTYPE_SPECIAL_CALL_DELEGATOR);
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
			if (zapsmethod(mi,minst)){   
					pht.removeMethod(minst);
					pht.addMethod(mi);
			} else if (zapsmethod(minst,mi)) {	
					skipped = true;
					}
				else { 
					       throw new InternalCompilerError("introduction of "+mi.getName()+
                                            " conflicts with an existing class member of " +pht);} 
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
	
	
	boolean zapsmethod(SootMethod mi1,SootMethod mi2) {
		if (!(Modifier.isAbstract(mi1.getModifiers())) && Modifier.isAbstract(mi2.getModifiers()))
			return true;
		// was mi2 then mi1
		if (!fromInterface(mi1) && fromInterface(mi2)) return true;
		if (!(isIntertype(mi1) && (isIntertype(mi2)))) return false;
		if (fromInterface(mi1) && fromInterface(mi2) &&
			 isSubInterface(interfaceTarget(mi1),interfaceTarget(mi2)))
			return true;
		return GlobalAspectInfo.v().getPrecedence(origin(mi1),origin(mi2)) == GlobalAspectInfo.PRECEDENCE_FIRST;    
	}
	
	private void addMethod( IntertypeMethodDecl imd ) {
		// System.out.println("add method "+imd.getTarget() + "from "+imd.getAspect() +
		//                   " and implementation " + imd.getImpl());
		SootMethod implMethod = addImplMethod(imd);
		addTargetMethod(imd,implMethod);
	}
	
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
		modifiers |= Modifier.PUBLIC;
		modifiers |= Modifier.STATIC; // the originating method is static
		modifiers &= ~Modifier.PRIVATE;
		modifiers &= ~Modifier.PROTECTED;
		
        
        if (!Modifier.isAbstract(modifiers)) {
		    // Create the method
		    SootMethod sm = new SootMethod( 
						  method.getName(),
						  parms,
						  retType,
						  modifiers );
	
			for( Iterator exceptionIt = method.getExceptions().iterator(); exceptionIt.hasNext(); ) {
				final SootClass exception = (SootClass) exceptionIt.next();
				sm.addException( exception );
			}
			
			sm.setSource(method.getSootMethod().getSource());
			
			sc.addMethod(sm);
			
			// System.out.println("real name is: " + MethodCategory.getName(sm));
			// System.out.println("added impl method " + sm);

			return sm;
        } else  return null;
	}
	
    private void addTargetMethod( IntertypeMethodDecl imd, SootMethod implMethod) {
        MethodSig method = imd.getTarget();
        
        SootClass sc = method.getDeclaringClass().getSootClass();
		 if( sc.isInterface() ) {
		 		Set implementors = hierarchy.getAllImplementersOfInterface(sc);
			   for( Iterator childClassIt = implementors.iterator(); childClassIt.hasNext(); ) {
				   final SootClass childClass = (SootClass) childClassIt.next();
				   if( childClass.isInterface() ) continue;
				   if( childClass.hasSuperclass() 
				   && implementors.contains(childClass.getSuperclass()) )
					   continue;

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
		            
		        // Create the method
		        SootMethod sm = new SootMethod( 
		                method.getName(),
		                parms,
		                retType,
		                modifiers );
		
		        for( Iterator exceptionIt = method.getExceptions().iterator(); exceptionIt.hasNext(); ) {
		
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
					InvokeExpr ie = Jimple.v().newStaticInvokeExpr(implMethod,args);
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
				MethodCategory.registerRealNameAndClass(sm, originalName, method.getDeclaringClass().getJavaName(),
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
				}
				s.add(pht);
			}
			return !skipped;
		}
	
		
	boolean zapsfield(SootField mi1,SootField mi2) {
			if (!(isIntertype(mi1) && isIntertype(mi2)))
				return false;
			return GlobalAspectInfo.v().getPrecedence(origin(mi1),origin(mi2)) == GlobalAspectInfo.PRECEDENCE_FIRST;    
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
		for( Iterator exceptionIt = method.getExceptions().iterator(); exceptionIt.hasNext(); ) {
				final SootClass exception = (SootClass) exceptionIt.next();
				sm.addException( exception );
		}
		cl.addMethod(sm);
		return sm;
	}
	
	private SootMethod getMethod(MethodSig getSig, SootField field,SootClass cl) {
		SootMethod sm = makeSootMethod(getSig,Modifier.PUBLIC,cl);
		//		create a body
		Body b = Jimple.v().newBody(sm); sm.setActiveBody(b);
		Chain ls = b.getLocals();
		PatchingChain ss = b.getUnits();
		//  target of the field reference is "this : targetType"
		SootClass sc = field.getDeclaringClass();
		RefType rt = sc.getType();
		ThisRef thisref = Jimple.v().newThisRef(rt);
		Local v = Jimple.v().newLocal("this$",rt); ls.add(v);
		IdentityStmt thisStmt = soot.jimple.Jimple.v().newIdentityStmt(v,thisref); ss.add(thisStmt);
		//  get the field we want to retrieve
		FieldRef sfref = Jimple.v().newInstanceFieldRef(b.getThisLocal(),field);
		// 	return the value
		Local r = Jimple.v().newLocal("result$",field.getType());  ls.add(r);
		AssignStmt rStmt = soot.jimple.Jimple.v().newAssignStmt(r, sfref); ss.add(rStmt);
		ReturnStmt stmt = Jimple.v().newReturnStmt(r); 
		ss.add(stmt);
		// This is an accessor method for reading a field
		MethodCategory.register(sm, MethodCategory.ACCESSOR_GET);
		MethodCategory.registerRealNameAndClass(sm, field.getName(), field.getDeclaringClass().getName(),
							0,0);
	    return sm;
	}
	
	private SootMethod setMethod(MethodSig getSig, SootField field,SootClass cl) {
		SootMethod sm = makeSootMethod(getSig,Modifier.PUBLIC,cl);
		//		create a body
		Body b = Jimple.v().newBody(sm); sm.setActiveBody(b);
		Chain ls = b.getLocals();
		PatchingChain ss = b.getUnits();
		//  target of the field reference is "this : targetType"
		SootClass sc = field.getDeclaringClass();
		RefType rt = sc.getType();
		ThisRef thisref = Jimple.v().newThisRef(rt);
		Local v = Jimple.v().newLocal("this$",rt); ls.add(v);
		IdentityStmt thisStmt = soot.jimple.Jimple.v().newIdentityStmt(v,thisref); ss.add(thisStmt);
		//  get the field we want to update
		FieldRef sfref = Jimple.v().newInstanceFieldRef(b.getThisLocal(),field);
		// 	get the parameter that we want to store
		Local p = Jimple.v().newLocal("param$",field.getType()); ; ls.add(p);
		ParameterRef pr = Jimple.v().newParameterRef(field.getType(),0);
		IdentityStmt prStmt = soot.jimple.Jimple.v().newIdentityStmt(p, pr); ss.add(prStmt);
		// now do the assignment
		AssignStmt rStmt = soot.jimple.Jimple.v().newAssignStmt(sfref, p); ss.add(rStmt);
		ReturnStmt stmt = Jimple.v().newReturnStmt(p); 
		ss.add(stmt);
		// This is an accessor method for writing a field
		MethodCategory.register(sm, MethodCategory.ACCESSOR_SET);
		MethodCategory.registerRealNameAndClass(sm, field.getName(), field.getDeclaringClass().getName(),
							0,0);
		return sm;
	}
	

    private void addField( IntertypeFieldDecl ifd ) {
        FieldSig field = ifd.getTarget();

        int modifiers = field.getModifiers();
        modifiers |= Modifier.PUBLIC;
        modifiers &= ~Modifier.PRIVATE;
        modifiers &= ~Modifier.PROTECTED;
        
       
        SootClass cl = field.getDeclaringClass().getSootClass();
        
        if( cl.isInterface() ) {
        	// add the accessor methods to the interface
        	
        	makeSootMethod(ifd.getGetter(),modifiers | Modifier.ABSTRACT,cl);
			makeSootMethod(ifd.getSetter(),modifiers | Modifier.ABSTRACT,cl);
        
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
                getMethod(ifd.getGetter(),newField,childClass);
                setMethod(ifd.getSetter(),newField,childClass);
                
                
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
		return GlobalAspectInfo.v().getPrecedence(origin(mi1),origin(mi2)) == GlobalAspectInfo.PRECEDENCE_FIRST;    
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
				SootMethod sa = ((MethodSig) arg).getSootMethod()	;			
				// now invoke the implementation in the originating aspect
				InvokeExpr ie = Jimple.v().newStaticInvokeExpr(sa,eiArgs);
				Local p = Jimple.v().newLocal("e"+index, sa.getReturnType());  ls.add(p);
				AssignStmt rStmt = soot.jimple.Jimple.v().newAssignStmt(p, ie); ss.add(rStmt);
				ccArgs.add(p); ccArgsTypes.add(sa.getReturnType());
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
		SootMethod constructor = ccReceiver.getMethod("<init>",ccArgsTypes);
	
		
		Expr ccallExpr = Jimple.v().newSpecialInvokeExpr(thisLoc,constructor,ccArgs);
		InvokeStmt ccall = Jimple.v().newInvokeStmt(ccallExpr);
		ss.add(ccall);
		
		// call static method aspect.body(this,f1,f2,...,fn)
		//  the first argument is "this"
		List bodyArgs = new ArrayList(eiArgs);
		bodyArgs.add(0,thisLoc);
		// System.out.println("getting method "+icd.getBody());
		SootMethod bodyMethod = icd.getBody().getSootMethod();
		Expr bodyExpr = Jimple.v().newStaticInvokeExpr(bodyMethod,bodyArgs);
		InvokeStmt body = Jimple.v().newInvokeStmt(bodyExpr);
		ss.add(body);
		
		//	return
		ReturnVoidStmt ret = Jimple.v().newReturnVoidStmt();
		ss.add(ret);
		
		
		// This is a stub for an intertype constructor decl
		MethodCategory.register(sm, MethodCategory.INTERTYPE_CONSTRUCTOR_DELEGATOR);
		MethodCategory.registerRealNameAndClass(sm, "<init>", scTarget.getName(),
							0,Modifier.isPublic(icd.getModifiers()) ? 0 : 1);//FIXME: Extra formals?
	}
	

    private boolean implementsInterface( SootClass child, SootClass iface ) {
        while(true) {
            if( child.getInterfaces().contains( iface ) ) return true;
            if( !child.hasSuperclass() ) return false;
            SootClass superClass = child.getSuperclass();
            if( superClass == child ) throw new RuntimeException( "Error: cycle in class hierarchy" );
            child = superClass;
        }
    }
    
	public void initialisers() {
		//	weave in intertype fields
			for( Iterator ifdIt = GlobalAspectInfo.v().getIntertypeFieldDecls().iterator(); ifdIt.hasNext(); ) {
				final IntertypeFieldDecl ifd = (IntertypeFieldDecl) ifdIt.next();
				initialiseField( ifd );
			}
		}
    
	private void initialiseField( IntertypeFieldDecl ifd ) {
		if (ifd.getInit() == null)
			return;
			FieldSig field = ifd.getTarget();

			int modifiers = field.getModifiers();
			modifiers |= Modifier.PUBLIC;
			modifiers &= ~Modifier.PRIVATE;
			modifiers &= ~Modifier.PROTECTED;

			Set targets = (Set) fieldITtargets.get(ifd);
			for (Iterator classIt = targets.iterator(); classIt.hasNext(); ) {
				SootClass cl = (SootClass) classIt.next();
				weaveInit(ifd,cl.getField(field.getName(),field.getType().getSootType()),modifiers, cl);
			}
		}

		private void weaveInit(
			IntertypeFieldDecl ifd,
			SootField sf,
			int modifiers,
			SootClass cl) {
			if (Modifier.isStatic(modifiers)) {
				// add a call to the initialiser method from clinit of target class
			    // if it doesn't exist, create one.	
					SootMethod clinit; Body b;
					try { 
						clinit = cl.getMethodByName("<clinit>");
						b = clinit.getActiveBody();
					} catch (java.lang.RuntimeException s) {
						clinit = new SootMethod( 
										  "<clinit>",   
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
				// get the method that initialises this field
				// which is a static method of the aspect that contains the ITD
					SootMethod initialiser = ifd.getInit().getSootMethod();
				// create the call
					List args = new ArrayList();
					InvokeExpr ie = Jimple.v().newStaticInvokeExpr(initialiser,args);
					Local res = Jimple.v().newLocal("result",initialiser.getReturnType()); b.getLocals().add(res);
					AssignStmt as = Jimple.v().newAssignStmt(res,ie); 
					ss.insertBefore(as,initstmt);
				// get the field we want to initialise
					FieldRef sfref = Jimple.v().newStaticFieldRef(sf); 
				//  assign the value
					AssignStmt rStmt = soot.jimple.Jimple.v().newAssignStmt(sfref, res); 
					ss.insertBefore(rStmt,initstmt);
			}
			else {
				// add a call to the initialiser method to any constructor of target class
				// that calls "super.<init>"
				for (Iterator ms = cl.methodIterator(); ms.hasNext(); ) {
					SootMethod clsm = (SootMethod) ms.next();
					if (clsm.getName() == "<init>") {
						Body b = clsm.getActiveBody();
						Chain ss = b.getUnits();
						// find unique super.<init>, throw exception if it's not unique
						Stmt initstmt = abc.soot.util.Restructure.findInitStmt(ss);
						// the following needs to be inserted right after initstmt
						if (initstmt.getInvokeExpr().getMethod().getDeclaringClass() == cl.getSuperclass()) {
							Chain units = b.getUnits();
							Stmt followingstmt = (Stmt) units.getSuccOf(initstmt);
						// 	the method that initialises this field
						//	which is a static method of the aspect that contains the ITD
							SootMethod sm = ifd.getInit().getSootMethod(); 
						//	now create the call
							List args = new ArrayList();
							args.add(b.getThisLocal()); // the only argument is "this"
							InvokeExpr ie = Jimple.v().newStaticInvokeExpr(sm,args); 
							Local res = Jimple.v().newLocal("result",sm.getReturnType()); b.getLocals().add(res);
							AssignStmt as = Jimple.v().newAssignStmt(res,ie); 
							units.insertBefore(as,followingstmt);
						//  get the field we want to initialise
							FieldRef sfref = Jimple.v().newInstanceFieldRef(b.getThisLocal(),sf);
						//  assign the value
							AssignStmt rStmt = soot.jimple.Jimple.v().newAssignStmt(sfref, res); 
							units.insertBefore(rStmt,followingstmt);
						}
					}
				}
			}
		}

}
