package abc.aspectj.ast;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;

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

import abc.aspectj.types.AspectJTypeSystem;
import abc.aspectj.types.InterTypeMethodInstance_c;
import abc.aspectj.types.InterTypeFieldInstance_c;

import abc.aspectj.types.AJContext;
import abc.aspectj.visit.*;
import abc.weaving.aspectinfo.FieldSig;

import abc.weaving.aspectinfo.MethodCategory;

public class IntertypeMethodDecl_c extends MethodDecl_c
    implements IntertypeMethodDecl, ContainsAspectInfo
{
    protected TypeNode host;
    public 	  InterTypeMethodInstance_c itMethodInstance;
    protected LocalInstance thisParamInstance;
    protected Supers supers;
    protected Flags origflags;

    public IntertypeMethodDecl_c(Position pos,
                                 Flags flags,
                                 TypeNode returnType,
                                 TypeNode host,
                                 String name,
                                 List formals,
                                 List throwTypes,
	  	                 Block body) {
	super(pos,flags,returnType,
              name,formals,throwTypes,body);
	this.host = host;
	this.supers = new Supers();
	this.origflags = flags;
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

    public NodeVisitor addMembersEnter(AddMemberVisitor am) {
		Type ht = host.type();
		if (ht instanceof ParsedClassType) {
			AspectJTypeSystem ts = (AspectJTypeSystem) am.typeSystem();
			
			MethodInstance mi = ts.interTypeMethodInstance(position(),
		                                	               	(ClassType) methodInstance().container(),
		                                               		(ReferenceType)ht,
		                                              		methodInstance().flags(),
		                                               		methodInstance().returnType(),
		                                               		methodInstance().name(),
		                                               		methodInstance().formalTypes(),
		                                               		methodInstance().throwTypes());
	    	((ParsedClassType)ht).addMethod(mi);
	    	// System.out.println("METHODS OF "+ht+"ARE "+ ((ParsedClassType) ht).methods());
	    	
	    	itMethodInstance = (InterTypeMethodInstance_c) mi;
	    	
	    	/* record instance for "this" parameter */
	    	String name = UniqueID.newID("this");
	    	thisParamInstance = ts.localInstance(position,Flags.NONE,host.type(),name);
		}
        return am.bypassChildren(this);
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
			return (IntertypeMethodDecl) name(mmi.name()).methodInstance(mmi).flags(mmi.flags());
		}
		return this;
	}
	
	/**
	 * introduce "this" as first parameter
	 * @author Oege de Moor
	 */
	public IntertypeDecl thisParameter(AspectJNodeFactory nf, AspectJTypeSystem ts) {	
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
			
			mi = mi.formalTypes(newtypes).flags(newflags);
		
			return (IntertypeDecl) formals(formals).flags(newflags).methodInstance(mi);
		} else 
			return this;
	}
	
	/**
	 * create a reference to the "this" parameter
	 * @author Oege de Moor
	 */
    public Expr thisReference(AspectJNodeFactory nf, AspectJTypeSystem ts) {
    	Local x = nf.Local(position,thisParamInstance.name());
    	x = (Local) x.localInstance(thisParamInstance).type(thisParamInstance.type());
    	return x;
    }

	public Node typeCheck(TypeChecker tc) throws SemanticException {
		if (flags().isProtected())
			throw new SemanticException("Intertype methods cannot be protected",position());
		if (flags().isStatic() && tc.context().currentClass().flags().isInterface())
			throw new SemanticException("Cannot declare static intertype method on interface",position());
		return super.typeCheck(tc);
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
		ncc.addITMembers(host.type().toClass());
		return ncc;		
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
	    formals.add(new abc.weaving.aspectinfo.Formal(AspectInfoHarvester.toAbcType(f.type().type()),
							  f.name(), f.position()));
	}
	List exc = new ArrayList();
	Iterator ti = throwTypes().iterator();
	while (ti.hasNext()) {
	    TypeNode t = (TypeNode)ti.next();
	    exc.add(t.type().toString());
	}
	abc.weaving.aspectinfo.MethodSig impl = new abc.weaving.aspectinfo.MethodSig
	    (AspectInfoHarvester.convertModifiers(flags()),
	     current_aspect.getInstanceClass(),
	     AspectInfoHarvester.toAbcType(returnType().type()),
	     name(),
	     formals,
	     exc,
	     position());
	abc.weaving.aspectinfo.MethodSig target = new abc.weaving.aspectinfo.MethodSig
	    (AspectInfoHarvester.convertModifiers(origflags),
	     gai.getClass(host.type()),
	     AspectInfoHarvester.toAbcType(returnType().type()),
	     name(),
	     formals,
	     exc,
	     null);
	abc.weaving.aspectinfo.IntertypeMethodDecl imd = new abc.weaving.aspectinfo.IntertypeMethodDecl
	    (target, impl, current_aspect, position());
	gai.addIntertypeMethodDecl(imd);
	gai.addSuperDispatches(supers.supercalls(gai));
	gai.addSuperFieldGetters(supers.superfieldgetters(gai));
	gai.addSuperFieldSetters(supers.superfieldsetters(gai));
	gai.addQualThiss(supers.qualthiss(gai));

	MethodCategory.register(this, MethodCategory.INTERTYPE_METHOD_SOURCE);
	MethodCategory.registerRealNameAndClass(this, name(), host.toString(),
						(flags().isStatic()?0:1),0);
    }
    
    public Supers getSupers() {
    	return supers;
    }
    
}
	

	

     


