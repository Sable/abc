/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
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

package abc.aspectj.ast;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

import polyglot.ast.Node;
import polyglot.ast.Block;
import polyglot.ast.TypeNode;
import polyglot.ast.Formal;
import polyglot.ast.Node;
import polyglot.ast.Expr;
import polyglot.ast.Local;
import polyglot.ast.ConstructorCall;
import polyglot.ast.Stmt;
import polyglot.ast.Return;
import polyglot.ast.MethodDecl;
import polyglot.ast.Call;
import polyglot.ast.Special;


import polyglot.util.CodeWriter;
import polyglot.util.UniqueID;
import polyglot.util.Position;

import polyglot.visit.*;
import polyglot.types.*;

import polyglot.ext.jl.ast.ConstructorDecl_c;
import polyglot.ext.jl.ast.Node_c;

import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.visit.*;
import abc.aspectj.types.AJContext;
import abc.aspectj.types.InterTypeConstructorInstance;
import abc.aspectj.types.InterTypeConstructorInstance_c;
import abc.aspectj.types.AspectType;

import abc.weaving.aspectinfo.AbcFactory;
import abc.weaving.aspectinfo.MethodCategory;
import abc.weaving.aspectinfo.GlobalAspectInfo;

/**
 * 
 *  @author Oege de Moor
 *  @author Aske Simon Christensen
 *
 */
public class IntertypeConstructorDecl_c extends ConstructorDecl_c
    implements IntertypeConstructorDecl, ContainsAspectInfo, MakesAspectMethods
{
    protected TypeNode host;
    protected LocalInstance thisParamInstance;
    protected String identifier;
    protected Flags originalFlags;
	
    public IntertypeConstructorDecl_c(Position pos,
                                 Flags flags,
                                 TypeNode host,
				 				 String name,
                                 List formals,
                                 List throwTypes,
	  	                 		Block body) {
	super(pos,flags,name,formals,throwTypes,body);
	this.host = host;
	this.identifier = UniqueID.newID("id");
	this.originalFlags = flags;
    }

	public TypeNode host() {
		return host;
	}
	
    protected IntertypeConstructorDecl_c reconstruct(List formals, 
						     List throwTypes, 
						     Block body,
						     TypeNode host) {
	if(host != this.host) {
	    IntertypeConstructorDecl_c n 
		= (IntertypeConstructorDecl_c) copy();
	    n.host = host;
	    return (IntertypeConstructorDecl_c) 
		n.reconstruct(formals,throwTypes,body);
	}
	return (IntertypeConstructorDecl_c) 
	    super.reconstruct(formals,throwTypes,body);
    }

    public Node visitChildren(NodeVisitor v) {
        List formals = visitList(this.formals, v);
        List throwTypes = visitList(this.throwTypes, v);
        Block body = (Block) visitChild(this.body, v);
		TypeNode host=(TypeNode) visitChild(this.host,v);
		return reconstruct(formals,throwTypes,body,host);
    }
    
    protected InterTypeConstructorInstance_c itConstructorInstance;
    
    /**
     * @author Aske Simon Christensen
     * @author Oege de Moor
     * add itd of methods to host types
     */
    public NodeVisitor addMembersEnter(AddMemberVisitor am) {
		Type ht = host.type();
		if (ht instanceof ParsedClassType) {
		   AJTypeSystem ts = (AJTypeSystem) am.typeSystem();
		   ConstructorInstance ci = ts.interTypeConstructorInstance(position(),identifier,
		   							(ClassType) constructorInstance().container(),
		   							(ClassType) ht,
		   							constructorInstance().flags(),
		   							constructorInstance().formalTypes(),
		   							constructorInstance().throwTypes());
		   
	  	  // ((ParsedClassType)ht).addConstructor(ci);
	  	  overrideITDconstructor((ParsedClassType)ht,ci);
	  	  itConstructorInstance = (InterTypeConstructorInstance_c) ci;
	  	  
		  /* record instance for "this" parameter */
		  String name = UniqueID.newID("this");
		  thisParamInstance = ts.localInstance(position,Flags.NONE,host.type(),name);
		}
        return am.bypassChildren(this);
    }
    
    
    static boolean hasConstructor(ClassType ct, ConstructorInstance ci) {
    	boolean res = false;
    	for (Iterator cit = ct.constructors().iterator(); !res && cit.hasNext(); ) {
    		ConstructorInstance cj = (ConstructorInstance) cit.next();
    		res = ci.hasFormals(cj.formalTypes());
    	}
    	return res;
    }
    
    static List constructors(ClassType ct, List formalTypes) {
    	List res = new ArrayList();
		for (Iterator cit = ct.constructors().iterator(); cit.hasNext(); ) {
			ConstructorInstance cj = (ConstructorInstance) cit.next();
			if (cj.hasFormals(formalTypes))
				res.add(cj);
		}
		return res;
	}
    
	public static void overrideITDconstructor(ClassType pht, 
											ConstructorInstance mi) {
			// System.out.println("attempting to add constructor "+mi+" to "+pht);
			InterTypeConstructorInstance_c itmic = (InterTypeConstructorInstance_c) mi;
			InterTypeConstructorInstance_c toinsert = (InterTypeConstructorInstance_c) mi.container(pht).flags(itmic.origFlags());
			// System.out.println("instance to insert:"+ " origin=" + toinsert.origin() +
			//										  " container=" + toinsert.container() +
			//										  " flags=" + toinsert.flags())	;
			boolean added = false;
			if (hasConstructor(pht,mi)) {
				// System.out.println("it has the constructor already");
				List mis = constructors(pht,mi.formalTypes());
				for (Iterator misIt = mis.iterator(); misIt.hasNext(); ) {
					// System.out.println("try next instance");
					ConstructorInstance minst = (ConstructorInstance) misIt.next();
					if (zaps(mi,minst) && !added){   
						pht.methods().remove(minst);
						pht.methods().add(toinsert);
						// System.out.println("replaced");
						added = true;
					} else if (zaps(minst,toinsert)) {	
						// skip  
						// System.out.println("skipped");
						}
					else if (!added) { pht.constructors().add(toinsert); added = true; 
										// System.out.println("added");
										} 
				}
			} else {pht.constructors().add(toinsert);
					added = true;
				// System.out.println("added");
				} 
			// System.out.println("exit overrideITDconstructor");
			if (added) 
				abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().registerWeave(AbcFactory.AbcClass(pht));
		}
	
		static boolean fromInterface(ConstructorInstance mi) {
			return mi instanceof InterTypeConstructorInstance &&
				   (((InterTypeConstructorInstance)mi).interfaceTarget() != null);
		}
	
		// replace this by a call to the appropriate structure!
		static boolean precedes(ClassType ct1, ClassType ct2) {
			return ct1.descendsFrom(ct2);
		}
	
		static boolean zaps(ConstructorInstance mi1,ConstructorInstance mi2) {
			if (!(mi1 instanceof InterTypeConstructorInstance &&
				  mi2 instanceof InterTypeConstructorInstance)) return false;
			InterTypeConstructorInstance itmi1 = (InterTypeConstructorInstance) mi1;
			InterTypeConstructorInstance itmi2 = (InterTypeConstructorInstance) mi2;
			return precedes(itmi1.origin(),itmi2.origin());	    
		}
    
	
    

	/**
	* @author Oege de Moor
	* change private intertype constructor decl into public,
	* mangling by giving it an extra parameter
	* 
	*/
	public IntertypeConstructorDecl accessChange(AJNodeFactory nf, AJTypeSystem ts) {
		if (flags().isPrivate() || flags().isPackage()){
			ParsedClassType ht = (ParsedClassType) host.type();
			ht.fields().remove(itConstructorInstance); // remove old instance from host type    		
			ConstructorInstance mmi = itConstructorInstance.mangled();  // retrieve the mangled instance 		
			ht.addConstructor(mmi); // add new instance to host type 
            List newFormals = new LinkedList(formals());
            newFormals.add(itConstructorInstance.mangledFormal(nf,ts));
			return (IntertypeConstructorDecl) constructorInstance(mmi).flags(mmi.flags()).formals(newFormals);
		}
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

	/**
	 * given an intertype constructor declaration of the form
	 *     A.new(formal1, ...,formaln) {
	 * 		ccall(E1,E2,...,Ek);  // optional call to super or this
	 *         init;
	 *     }
	 * we want to transform it into the following shape
	 * 	   A.new(formal1, ..., formaln) {
	 *         receiver.ccall(e1(formal1,...,formaln), ..., ek(formal1,...,formaln)); // no longer optional
	 * 		body(this,formal1,...,formaln);
	 *     }
	 * where e1,...,ek and body are newly generated methods in the 
	 * originating aspect.
	 * 
	 * We detect the special case that one of the original Ei is 
	 * just a reference to a formal; in that case no method is lifted out.
	 * 
	 * The generated methods are appended onto the list that is the last
	 * parameter.
	 * 
	 * Because we aim not to override the Jimplifier behaviour, it is not possible
	 * to Jimplify the generated constructor: references to this and super would 
	 * refer to the originating aspect, not the target. For that reason we record the 
	 * following information, which is later stored in the AspectInfo structure:
	 *
     * 	- formal types:    						List<Type> aiFormalTypes
	 *   	- qualifier of the ccall				ReferenceType aiQualifier  // may be null
	 *     - kind of the ccall						ConstructorCall.Kind aiKind
	 *    	- list of ei method instances
	 *           or parameter positions		List<Integer | MethodInstance> aiEis
	 *     - body method instance			MethodInstance aiBody
	 * 
	 * Using this information, the weaver (more precisely, the IntertypeAdjuster)
	 * will generate code for the constructor in the target class.
	 * 
	 * @author Oege de Moor
	 */
	public IntertypeConstructorDecl liftMethods(AJNodeFactory nf, AJTypeSystem ts, List methodDecls) {
		ConstructorCall ccall = findCCall();
		Block b = nf.Block(position);
		if (ccall != null) {
			List Es = ccall.arguments();
			List es = new LinkedList();
			for (Iterator Eis = Es.iterator(); Eis.hasNext(); ) {
				Expr Ei = (Expr) Eis.next();
				if (Ei instanceof Local) 
					es.add(Ei); 
				else  es.add(genArgMethod(nf,ts,Ei,methodDecls));
			}
			ccall = (ConstructorCall) ccall.arguments(es); // replace the Ei by ei
			b = b.append(ccall);
		} else { // create a call to super()
			ccall = nf.ConstructorCall(position(),ConstructorCall.SUPER,new LinkedList());
			ConstructorInstance cci = ts.constructorInstance(position(),host().type().toClass(),Flags.PUBLIC,new LinkedList(),throwTypes());
			ccall = ccall.constructorInstance(cci);
		}
		setAspectInfoCCall(ccall);
		List init = withoutCCall(); 
		Call bodyCall = genBodyMethod(nf,ts,formals,init,methodDecls);
		setAspectInfoBody(bodyCall);
		b = b.append(nf.Eval(position,bodyCall));
		return (IntertypeConstructorDecl) body(b);  
	}
	
	
	/**
	 * find the call to "super(..)" or "this(..)", if one exists.
	 * It must be the first statement of the body.
	 * @author Oege de Moor
	 */
	private ConstructorCall findCCall() {
		Block b = body();
		Stmt stmt = (Stmt) b.statements().get(0);
		if (stmt instanceof ConstructorCall)
			return (ConstructorCall) stmt;
		else return null;
	}
	
	/**
	 * the statements in the constructor body, without
	 * the initial constructor call (if one exists)
	 * @author Oege de Moor
	 */
	private List withoutCCall() {
		List result = new LinkedList(body().statements());
		if (result.get(0) instanceof ConstructorCall)
			result.remove(0);
		return result;
	}
	
	/**
	 * generate a static method for the given expression,
	 * in the originating aspect, and return a call to that
	 * static method. The declaration of the generated method
	 * is appended onto the last parameter.
	 * @author Oege de Moor
	 */
	private Expr genArgMethod(AJNodeFactory nf, AJTypeSystem ts, Expr Ei,List methodDecls) {
		String name = UniqueID.newID("arg$"+name());
		
		// build the formals: just a copy of the existing formals
		List newFormals = new LinkedList(formals);
		
		// build the body
		Return ret = nf.Return(Ei.position(),Ei);
		Block b = nf.Block(position,ret);
		
		// build the methodinstance
		List argTypes = new LinkedList(constructorInstance().formalTypes());
		List excTypes = Ei.throwTypes(ts);
		MethodInstance mi = ts.methodInstance(position,itConstructorInstance.origin(),flags,Ei.type(),name,argTypes,excTypes);
		
		// build the declaration
		TypeNode rettype = nf.CanonicalTypeNode(position,mi.returnType());
		MethodDecl md = nf.MethodDecl(position,Flags.STATIC,rettype,name,newFormals,excTypes,b);
		md = md.methodInstance(mi);
		
		// record the method declaration
		methodDecls.add(md);
		
		// construct the call
		List actuals = new LinkedList();
		for(Iterator nfs = formals().iterator(); nfs.hasNext(); ) {
				Formal f = (Formal) nfs.next();
				Type t = f.type().type();
				Local a = nf.Local(f.position(),f.name()).localInstance(f.localInstance());
				a = (Local) a.type(t);
				actuals.add(a);
		}
		TypeNode targetTypeNode = nf.CanonicalTypeNode(position, constructorInstance().container());
		Call c = nf.Call(position(),targetTypeNode,name,actuals);
		c = c.methodInstance(mi);
		
		return c;
	}

	private void buildATypes(
		AJNodeFactory nf,
		List newFormals,
		List actuals,
		List argTypes) {
		Iterator nfs = newFormals.iterator();
		Formal f = (Formal) nfs.next();
		Type t = f.type().type();
		
		TypeNode tn = nf.CanonicalTypeNode(position,t);
		Expr a = nf.This(position,tn);
		a = a.type(t);
		
		actuals.add(a);
		argTypes.add(t);
		while (nfs.hasNext()) {
			f = (Formal) nfs.next();
			t = f.type().type();
			a = nf.Local(f.position(),f.name()).localInstance(f.localInstance());
			a = a.type(t);
			actuals.add(a);
			argTypes.add(t);
		}
	}

	private List buildFormals(AJNodeFactory nf) {
		List newFormals;
		newFormals = new LinkedList(formals);
		
		LocalInstance thisInstance = thisParamInstance; // fishy? same instance for multiple methods...
		TypeNode tn = nf.CanonicalTypeNode(position,host.type());
		tn = tn.type(host.type());
		Formal thisp = nf.Formal(position,Flags.FINAL,tn,thisInstance.name());
		thisp = thisp.localInstance(thisInstance);
		newFormals.add(0,thisp); 
		
		return newFormals;
	}
	
	/**
	 * generate a static method for the body, in the originating
	 * aspect. 
	 */
	private Call genBodyMethod(AJNodeFactory nf, AJTypeSystem ts, List formals, List stmts, List methodDecls){
		String name = UniqueID.newID("new$"+name());
		List newFormals = buildFormals(nf);

		// build the body
		Block b = nf.Block(position,stmts);
		
		// build the methodinstance
		List actuals = new LinkedList();
		List argTypes = new LinkedList();
		buildATypes(nf, newFormals, actuals, argTypes);
		List excTypes = throwTypes(ts);
		MethodInstance mi = ts.methodInstance(position,itConstructorInstance.origin(),flags,ts.Void(),name,argTypes,excTypes);
		
		// build the declaration
		TypeNode rettype = nf.CanonicalTypeNode(position,mi.returnType());
		MethodDecl md = nf.MethodDecl(position,Flags.STATIC,rettype,name,newFormals,excTypes,b);
		md = md.methodInstance(mi);
		
		// record the method declaration
		methodDecls.add(md);
		
		// construct the call
		TypeNode targetTypeNode = nf.CanonicalTypeNode(position, constructorInstance().container());
		Call c = nf.Call(position(),targetTypeNode,name,actuals);
		c = c.methodInstance(mi);
		
		return c; 
	}

    /** Duplicate most of the things for ConstructorDecl here to avoid comparing
     *  the name against the contaning class.
     */
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        Context c = tc.context();
        TypeSystem ts = tc.typeSystem();

        // hostClass in lieu of currentClass
        ClassType ct = ((AJContext) c).hostClass();

		if (ct.flags().isInterface()) {
	    	throw new SemanticException(
			"Cannot declare an intertype constructor on an interface.",
			position());
		}
		
		if (flags().isProtected()) {
			throw new SemanticException("Cannot declare a protected intertype constructor");
		}
		

        if (ct.isAnonymous()) {
	    throw new SemanticException(
		"Cannot declare an intertype constructor on an anonymous class.",
		position());
        }

	/*
        String ctName = ct.name();

        if (! ctName.equals(name)) {
	    throw new SemanticException("Constructor name \"" + name +
                "\" does not match name of containing class \"" +
                ctName + "\".", position());
        }
	*/

	try {
	    ts.checkConstructorFlags(flags());
	}
	catch (SemanticException e) {
	    throw new SemanticException(e.getMessage(), position());
	}

	if (body == null && ! flags().isNative()) {
	    throw new SemanticException("Missing constructor body.",
		position());
	}

	if (body != null && flags().isNative()) {
	    throw new SemanticException(
		"A native constructor cannot have a body.", position());
	}

        for (Iterator i = throwTypes().iterator(); i.hasNext(); ) {
            TypeNode tn = (TypeNode) i.next();
            Type t = tn.type();
            if (! t.isThrowable()) {
                throw new SemanticException("Type \"" + t +
                    "\" is not a subclass of \"" + ts.Throwable() + "\".",
                    tn.position());
            }
        }

		if ((ct instanceof AspectType) &&
					constructorInstance().formalTypes().size() > 0)
					throw new SemanticException("Aspects can only have nullary constructors",position());
					
        return this;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.begin(0);
	w.write(flags.translate());
        print(host,w,tr);
        w.write(".new("); 

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
    
    
	/**
	* @author Oege de Moor
	* record the host type in the environment, for checking of this and super.
	* also add fields and methods of the host that are visible from the aspect.
	*/
	
	public Context enterScope(Context c) {
		AJContext nc = (AJContext) super.enterScope(c);
		TypeSystem ts = nc.typeSystem();
		AJContext ncc = (AJContext) nc.pushHost(ts.staticTarget(host.type()).toClass(),
											   flags.isStatic());
		return ncc.addITMembers(host.type().toClass());
	}
    
	private List /*<Type>*/ aiFormalTypes;
	private ReferenceType aiQualifier;  // may be null
	private ConstructorCall.Kind aiKind;
	private List /* <Integer | MethodInstance> */ aiEis;
	private MethodInstance aiBody;

	private void setAspectInfoCCall(ConstructorCall cc) {
		aiQualifier = (cc.qualifier() != null ? cc.qualifier().type().toReference() : null);
		aiKind = cc.kind();
		aiEis = new ArrayList();
		aiFormalTypes = new ArrayList(constructorInstance().formalTypes());
		for (Iterator fs = cc.arguments().iterator(); fs.hasNext(); ){
			Expr e = (Expr) fs.next();
			if (e instanceof Local)
				aiEis.add(new Integer(indexof((Local) e)));
			if (e instanceof Call)
				aiEis.add(((Call) e).methodInstance());
		}
	}
	
	private int indexof(Local e) {
		int index = 0;
		for (Iterator fs = formals.iterator(); fs.hasNext(); ) {
			Formal f = (Formal) fs.next();
			if (f.localInstance().equals( e.localInstance())) 
				return index;
			index++;
		}
		return 0; // should not happen
	}

	private void setAspectInfoBody(Call c) {
			aiBody = c.methodInstance();
	}


	public void update(abc.weaving.aspectinfo.GlobalAspectInfo gai, abc.weaving.aspectinfo.Aspect current_aspect) {
	// System.out.println("ICD host: "+host.toString());
		abc.weaving.aspectinfo.AbcClass target = AbcFactory.AbcClass((ClassType)host.type());
		int mod = AbcFactory.modifiers(flags());
		int origmod = AbcFactory.modifiers(originalFlags);
		List formalTypes = new ArrayList();
		Iterator fi = aiFormalTypes.iterator();
		int index = 0;
		while (fi.hasNext()) {
    		Type f = (Type) fi.next();
    		formalTypes.add(AbcFactory.AbcType(f));
			index++;
		}
		List exc = new ArrayList();
		Iterator ti = throwTypes().iterator();
		while (ti.hasNext()) {
    		TypeNode t = (TypeNode)ti.next();
    		exc.add(t.type().toString());
		}
		abc.weaving.aspectinfo.AbcClass qualifier = (aiQualifier == null ? null : AbcFactory.AbcClass((ClassType)aiQualifier));
		int kind = (aiKind == ConstructorCall.SUPER ? 
							abc.weaving.aspectinfo.IntertypeConstructorDecl.SUPER :
							abc.weaving.aspectinfo.IntertypeConstructorDecl.THIS);
		List arguments = new ArrayList();
		for (Iterator args = aiEis.iterator(); args.hasNext(); ) {
			Object arg = args.next();
			if (arg instanceof Integer)
				arguments.add(arg);
			if (arg instanceof MethodInstance) {
				MethodInstance mi = (MethodInstance) arg;
				abc.weaving.aspectinfo.MethodSig sig = AbcFactory.MethodSig(mi);
				arguments.add(sig);
				MethodCategory.register(sig, MethodCategory.INTERTYPE_CONSTRUCTOR_SPECIAL_ARG);
			}
		}
		abc.weaving.aspectinfo.MethodSig body = AbcFactory.MethodSig(aiBody);
		abc.weaving.aspectinfo.IntertypeConstructorDecl icd = new abc.weaving.aspectinfo.IntertypeConstructorDecl
    		(target, current_aspect, mod, origmod, originalFlags.isPrivate() || originalFlags.isPackage(), formalTypes, exc, qualifier, kind, arguments, body, position());
		gai.addIntertypeConstructorDecl(icd);

		MethodCategory.register(body, MethodCategory.INTERTYPE_CONSTRUCTOR_BODY);
		// FIXME: First argument is this, right?
		MethodCategory.registerRealNameAndClass(body, AbcFactory.modifiers(originalFlags), "<init>", current_aspect.getInstanceClass(),
							1,0);
	}
 
        public void aspectMethodsEnter(AspectMethods visitor)
        {
                visitor.pushIntertypeDecl(this);
        }

        public Node aspectMethodsLeave(AspectMethods visitor, AJNodeFactory nf,
                                       AJTypeSystem ts)
        {
                visitor.popIntertypeDecl();
                IntertypeConstructorDecl_c itcd = (IntertypeConstructorDecl_c) accessChange(nf, ts);
                return itcd.liftMethods(nf, ts, visitor.methods());
        }
}
