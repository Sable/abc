/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Oege de Moor
 * Copyright (C) 2004 Aske Simon Christensen
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

package abc.aspectj.ast;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

import polyglot.ast.Block;
import polyglot.ast.TypeNode;
import polyglot.ast.Formal;
import polyglot.ast.Node;
import polyglot.ast.Expr;
import polyglot.ast.Local;
import polyglot.ast.Call;
import polyglot.ast.Field;
import polyglot.ast.Return;

import polyglot.util.CodeWriter;
import polyglot.util.UniqueID;
import polyglot.util.Position;
import polyglot.util.InternalCompilerError;


import polyglot.visit.*;
import polyglot.types.*;

import polyglot.ext.jl.ast.MethodDecl_c;
import polyglot.ext.jl.types.TypeSystem_c;

import abc.aspectj.ExtensionInfo;
import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.types.AJTypeSystem_c;
import abc.aspectj.types.InterTypeMethodInstance;
import abc.aspectj.types.InterTypeMethodInstance_c;
import abc.aspectj.types.InterTypeFieldInstance_c;

import abc.aspectj.types.AJContext;
import abc.aspectj.types.AJFlags;
import abc.aspectj.visit.*;
import abc.weaving.aspectinfo.FieldSig;
import abc.weaving.aspectinfo.GlobalAspectInfo;

import abc.weaving.aspectinfo.AbcFactory;
import abc.weaving.aspectinfo.MethodCategory;

/**
 * 
 *  @author Oege de Moor
 *  @author Aske Simon Christensen
 *
 */
public class IntertypeMethodDecl_c extends MethodDecl_c
    implements IntertypeMethodDecl, ContainsAspectInfo, MakesAspectMethods
{
    protected TypeNode host;
    public 	  InterTypeMethodInstance itMethodInstance;
    protected LocalInstance thisParamInstance;
    protected Flags origflags;
    protected String identifier;
    protected String originalName;
    protected List /*<MethodInstance>*/ derivedMis;

    public IntertypeMethodDecl_c(Position pos,
                                 Flags flags,
                                 TypeNode returnType,
                                 TypeNode host,
                                 String name,
                                 List formals,
                                 List throwTypes,
	  	                 Block body) {	
	
	super(pos,AJFlags.intertype(flags),returnType,
              name,formals,throwTypes,body);
	this.host = host;
	this.origflags = flags;
	this.identifier = UniqueID.newID("id");
	this.originalName = name;
	this.derivedMis = new ArrayList();
    }

	public TypeNode host() {
		return host;
	}
	
    protected IntertypeMethodDecl_c reconstruct(TypeNode returnType, 
						List formals, 
						List throwTypes, 
						Block body,
						TypeNode host) {
	if(host != this.host) {
	    IntertypeMethodDecl_c n =
		(IntertypeMethodDecl_c) copy();
	    n.host=host;
	    return (IntertypeMethodDecl_c) 
		n.reconstruct(returnType,formals,throwTypes,body);
	}
	return (IntertypeMethodDecl_c)
	    super.reconstruct(returnType,formals,throwTypes,body);
    }

    public Node visitChildren(NodeVisitor v) {
        List formals = visitList(this.formals, v);
        TypeNode returnType = (TypeNode) visitChild(this.returnType, v);
        List throwTypes = visitList(this.throwTypes, v);
        Block body = (Block) visitChild(this.body, v);
	    TypeNode host = (TypeNode) visitChild(this.host, v);
	    return reconstruct(returnType,formals,throwTypes,body,host);
    }

	public void addDerived(MethodInstance mi) {
		derivedMis.add(mi);
	}
	
    public NodeVisitor addMembersEnter(AddMemberVisitor am) {
		Type ht = host.type();
		if (ht instanceof ParsedClassType) {
			ParsedClassType pht = (ParsedClassType) ht;
			
			AJTypeSystem ts = (AJTypeSystem) am.typeSystem();
			
			Flags newFlags = flags();
			if (pht.flags().isInterface()) {
				newFlags = newFlags.Abstract();
			    if (origflags.isAbstract() && !origflags.isPrivate()) {
					newFlags = newFlags.Public();
					origflags = origflags.Public();
				} 
			}
			MethodInstance mi = ts.interTypeMethodInstance(position(), identifier,
		                                	               	(ClassType) methodInstance().container(),
		                                               		(ReferenceType)ht,
		                                               		newFlags,
		                                              		origflags,
		                                               		methodInstance().returnType(),
		                                               		methodInstance().name(),
		                                               		methodInstance().formalTypes(),
		                                               		methodInstance().throwTypes());                                			    
		   overrideITDmethod(pht, mi);
	    	
    	
	    	itMethodInstance = (InterTypeMethodInstance) mi;
	    	
	    	/* record instance for "this" parameter */
	    	String name = UniqueID.newID("this");
	    	thisParamInstance = ts.localInstance(position,Flags.FINAL,host.type(),name);
		}
        return am.bypassChildren(this);
    }


	public static void overrideITDmethod(ClassType pht, 
											MethodInstance mi) {
	    // System.out.println("attempting to add method "+mi+" to "+pht);
		InterTypeMethodInstance_c toinsert = (InterTypeMethodInstance_c) mi;
		// InterTypeMethodInstance_c toinsert =  (InterTypeMethodInstance_c) mi.container(pht).flags(itmic.origFlags());
		// System.out.println("instance to insert:"+ " origin=" + toinsert.origin() +
		//                                          " container=" + toinsert.container() +
		//                                          " flags=" + toinsert.flags())	;
		boolean added = false;
		if (pht.hasMethod(mi)) {
			// System.out.println("it has the method already");
			List mis = pht.methods(mi.name(),mi.formalTypes());
			for (Iterator misIt = mis.iterator(); misIt.hasNext(); ) {
				MethodInstance minst = (MethodInstance) misIt.next();
				if (zaps(mi,minst) && !added){   
					pht.methods().remove(minst);
					pht.methods().add(toinsert);
					// System.out.println("replaced");
					added = true;
				} else if (zaps(minst,toinsert)) {	
					// skip  
					// System.out.println("skipped");
					}
				else if (!added) { pht.methods().add(toinsert); added = true; 
									// System.out.println("added1");
									} 
			}
		} else {pht.methods().add(toinsert); added=true; // System.out.println("added2");
					} 
		if (added)
			abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().registerWeave(AbcFactory.AbcClass(pht));
		// System.out.println("exit overrideITDmethod");
	}

	static boolean comparable(ClassType ct1, ClassType ct2) {
		return ct1.equals(ct2) || ct1.descendsFrom(ct2) || ct2.descendsFrom(ct1);
	}
	
	
	/** Check whether an intertype declaration via an interface conflicts with a declaration in
	 *  a parent. Not very pretty; should be simplified. ODM 09/07/04
	 * 
	 * */
	static public void conflictWithParentCheck(InterTypeMethodInstance_c mi) throws SemanticException {
		ClassType pht = mi.container().toClass(); // host class
		AJTypeSystem_c ts = (AJTypeSystem_c) mi.typeSystem();
		MethodInstance mj = null;
		pht.methods().remove(mi);
		try {
		   mj = ts.findMethod(pht,mi.name(),mi.formalTypes(),pht);
		} catch (SemanticException e) {};
		pht.methods().add(mi);
		if ((mj != null) && 
		    !zaps(mi,mj) &&
		    (!mj.container().equals(pht)) && 
		    fromInterface(mi) && !comparable(mj.container().toClass(),mi.interfaceTarget())) {
			if (mj instanceof InterTypeMethodInstance_c) {
				InterTypeMethodInstance_c itmj = (InterTypeMethodInstance_c) mj;
				throw new SemanticException ("Intertype method "+itmj.name()+" introduced by aspect "+itmj.origin() + " into " +
				                         itmj.container() + " (a superclass of "+ pht +") conflicts with introduction by aspect "+mi.origin() + 
                                         " into " + mi.interfaceTarget() + " which is implemented by" + pht, mi.position());
			}
			   throw new SemanticException("Intertype method " + mi.name() + " introduced by aspect "+ mi.origin() + " into " +
			                        mi.interfaceTarget() + " (which is an interface of "+pht+") conflicts with existing member of " +
			                        mj.container() + " (which is a superclass of "+pht+")",mi.position());
		}
	}

    /** Do the usual override check for newly declared methods.  Probably better to more
     * this to AJTypeSystem. */
	static public void overrideMethodCheck(MethodInstance mi) throws SemanticException {
		   AJTypeSystem_c ts = (AJTypeSystem_c) mi.typeSystem();
		   for (Iterator j = mi.implemented().iterator(); j.hasNext(); ) {
			   MethodInstance mj = (MethodInstance) j.next();
			   if (! ts.isAccessible(mj, mi.container().toClass())) {
				   continue;
			   }
			    // MethodInstance mi2 = mi.flags(mi.origFlags());
				MethodInstance mj2;
				if (mj instanceof InterTypeMethodInstance_c) {
					InterTypeMethodInstance mji = (InterTypeMethodInstance) mj;
				    mj2 = mj.flags(mji.origFlags());
				}
				else
					{mj2 = mj;} 
				
			   ts.checkOverride(mi, mj);
		   }
	}
	
	static public void intertypeMethodChecks(ClassType ct) throws SemanticException {
		List copyOfMethods = new LinkedList(ct.methods());
		for (Iterator metIt = copyOfMethods.iterator(); metIt.hasNext(); ) {
			MethodInstance mi = (MethodInstance) metIt.next();
			if (mi instanceof InterTypeMethodInstance_c) {
				InterTypeMethodInstance_c itmi = (InterTypeMethodInstance_c) mi;
				conflictWithParentCheck(itmi);
				overrideMethodCheck(itmi);
			}
		}
	}
	
	static boolean fromInterface(MethodInstance mi) {
		return ((mi instanceof InterTypeMethodInstance_c &&
		       (((InterTypeMethodInstance)mi).interfaceTarget() != null)));
	}
	
	// replace this by a call to the appropriate structure!
	static boolean precedes(ClassType ct1, ClassType ct2) {
		return (ct1.descendsFrom(ct2)  || 
					  (abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getPrecedence(ct1.fullName(),
					                                      ct2.fullName()) 
					    == GlobalAspectInfo.PRECEDENCE_FIRST) );
					
	}
	
	static boolean zaps(MethodInstance mi1,MethodInstance mi2) {
		if (!(mi1.flags().isAbstract()) && mi2.flags().isAbstract())
			return true;
		if (mi1 instanceof InterTypeMethodInstance_c && mi2.container().toClass().flags().isInterface())
			return true;
		//System.out.println("not (!mi1.abstract && mi2.abstract)");
		// was mi2 then mi1
 	    if (!fromInterface(mi1) && fromInterface(mi2)) return true;
 		// System.out.println("not (!mi2.fromInterface && mi1.fromInterface");
		if (!(mi1 instanceof InterTypeMethodInstance_c &&
		      mi2 instanceof InterTypeMethodInstance_c)) return false;
		// System.out.println("check descendency");
		InterTypeMethodInstance_c itmi1 = (InterTypeMethodInstance_c) mi1;
		InterTypeMethodInstance_c itmi2 = (InterTypeMethodInstance_c) mi2;
		if (fromInterface(itmi1) && fromInterface(itmi2) &&
		     itmi1.interfaceTarget().descendsFrom(itmi2.interfaceTarget()))
		    return true;
		return precedes(itmi1.origin(),itmi2.origin());	    
	}
    
	
	/**
	* @author Oege de Moor
	* change private intertype method decl into public,
	* mangling the name.
	*/
	public IntertypeMethodDecl accessChange() {
		if (flags().isPrivate() || flags().isPackage()) {
			ParsedClassType ht = (ParsedClassType) host.type();
			ht.methods().remove(itMethodInstance); // remove old instance from host type    		
			MethodInstance mmi = itMethodInstance.mangled();  // retrieve the mangled instance 		
			ht.addMethod(mmi); // add new instance to host type
			Flags newFlags = mmi.flags();   	
			return (IntertypeMethodDecl) name(mmi.name()).methodInstance(mmi);
		}
		return this;
	}
	
	/**
	 * introduce "this" as first parameter
	 * @author Oege de Moor
	 */
	public IntertypeDecl thisParameter(AJNodeFactory nf, AJTypeSystem ts) {	
		if (!flags().isStatic()) {
			// create the new list of formals
			TypeNode tn = nf.CanonicalTypeNode(position,thisParamInstance.type());
			Formal newformal = nf.Formal(position,thisParamInstance.flags(),tn,thisParamInstance.name());
			newformal = newformal.localInstance(thisParamInstance);
			List formals = new LinkedList(formals());
			formals.add(0,newformal);
			
			// create the new methodinstance
			MethodInstance mi = methodInstance();
			List newtypes = new LinkedList(mi.formalTypes());
			newtypes.add(0,thisParamInstance.type());
			
			Flags newflags = mi.flags().set(Flags.STATIC);
			
			if (!(itMethodInstance.origFlags().isAbstract()))
				newflags = newflags.clear(Flags.ABSTRACT);
			// System.out.println("the new flags for the implementation are " + newflags);
			
			mi = mi.formalTypes(newtypes).flags(newflags);
		
			return (IntertypeDecl) formals(formals).flags(newflags).methodInstance(mi);
		} else 
			return this;
	}
	
	/**
	 * create a reference to the "this" parameter
	 * @author Oege de Moor
	 */
    public Expr thisReference(AJNodeFactory nf, AJTypeSystem ts) {
    	Local x = nf.Local(position,thisParamInstance.name());
    	x = (Local) x.localInstance(thisParamInstance).type(thisParamInstance.type());
    	return x;
    }

	public Node typeCheck(TypeChecker tc) throws SemanticException {
		if (flags().isProtected())
			throw new SemanticException("Intertype methods cannot be protected",position());
		if (flags().isStatic() && host.type().toClass().flags().isInterface())
			throw new SemanticException("Cannot declare static intertype method on interface",position());
		if (host.type() instanceof ParsedClassType &&
		    !abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getWeavableClasses()
		    .contains(abc.weaving.aspectinfo.AbcFactory.AbcClass((ParsedClassType) host.type())))
		    throw new SemanticException("Host of an intertype declaration must be a weavable class");

		return super.typeCheck(tc);
	}
	
	/**
	 * @author Oege de Moor
	 * record the host type in the environment, for checking of this and super.
	 * also add fields and methods of the host that are visible from the aspect.
	 */
	
	public Context enterScope(Context c) {
		// System.out.println("entering scope of "+name+" in class "+c.currentClass());
			AJContext nc = (AJContext) super.enterScope(c);
			TypeSystem ts = nc.typeSystem();
			AJContext ncc = (AJContext) nc.pushHost(ts.staticTarget(host.type()).toClass(),
				                               flags.isStatic());
			return ncc.addITMembers(host.type().toClass());
		// System.out.println("current class="+ncc.currentClass());
	}
	
	
	
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.begin(0);
	w.write(flags.translate());
        print(returnType, w, tr);
        w.write(" ");
        print(host,w,tr);
        w.write("." + name + "("); 

        w.begin(0);

	for (Iterator i = formals.iterator(); i.hasNext(); ) {
	    Formal f = (Formal) i.next();
	    print(f, w, tr);

	    if (i.hasNext()) {
		w.write(",");
		w.allowBreak(0, " ");
	    }
	}

	w.end();

	w.write(")");

	w.begin(0);

        if (! throwTypes().isEmpty()) {
	    w.allowBreak(6);
	    w.write("throws ");

	    for (Iterator i = throwTypes().iterator(); i.hasNext(); ) {
	        TypeNode tn = (TypeNode) i.next();
		print(tn, w, tr);

		if (i.hasNext()) {
		    w.write(",");
		    w.allowBreak(4, " ");
		}
	    }
	}

	w.end();

	if (body != null) {
	    printSubStmt(body, w, tr);
	}
	else {
	    w.write(";");
	}

	w.end();

    }

    public void update(abc.weaving.aspectinfo.GlobalAspectInfo gai, abc.weaving.aspectinfo.Aspect current_aspect) {
	// System.out.println("IMD host: "+host.toString());
	List formals = new ArrayList();
	Iterator fi = formals().iterator();
	while (fi.hasNext()) {
	    Formal f = (Formal)fi.next();
	    formals.add(new abc.weaving.aspectinfo.Formal(AbcFactory.AbcType(f.type().type()),
							  f.name(), f.position()));
	}
	List exc = new ArrayList();
	Iterator ti = throwTypes().iterator();
	while (ti.hasNext()) {
	    TypeNode t = (TypeNode)ti.next();
	    exc.add(AbcFactory.AbcClass((ClassType)t.type()));
	}
	abc.weaving.aspectinfo.MethodSig impl = new abc.weaving.aspectinfo.MethodSig
	    (AbcFactory.modifiers(flags()),
	     current_aspect.getInstanceClass(),
	     AbcFactory.AbcType(returnType().type()),
	     name(),
	     formals,
	     exc,
	     position());
	abc.weaving.aspectinfo.MethodSig target = new abc.weaving.aspectinfo.MethodSig
	    (AbcFactory.modifiers(origflags),
	     AbcFactory.AbcClass((ClassType)host.type()),
	     AbcFactory.AbcType(returnType().type()),
	     name(),
	     formals,
	     exc,
	     null);
	abc.weaving.aspectinfo.IntertypeMethodDecl imd = new abc.weaving.aspectinfo.IntertypeMethodDecl
	    (target, impl, current_aspect, originalName, position());
	gai.addIntertypeMethodDecl(imd);
	
	MethodCategory.register(impl, MethodCategory.INTERTYPE_METHOD_SOURCE);
	MethodCategory.registerRealNameAndClass(impl, AbcFactory.modifiers(origflags), originalName, AbcFactory.AbcClass((ClassType)host.type()),
						(origflags.isStatic()?0:1),0);
    }
    
    public void aspectMethodsEnter(AspectMethods visitor)
    {
        visitor.pushIntertypeDecl(this);
    }

    public Node aspectMethodsLeave(AspectMethods visitor, AJNodeFactory nf,
                                   AJTypeSystem ts)
    {
        visitor.popIntertypeDecl();
        return ((IntertypeMethodDecl_c) this.accessChange()).thisParameter(nf,ts);
    }
}
