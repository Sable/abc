package abc.aspectj.ast;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import polyglot.ast.Block;
import polyglot.ast.TypeNode;
import polyglot.ast.Formal;
import polyglot.ast.Field;
import polyglot.ast.Local;
import polyglot.ast.Return;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.Assign;
import polyglot.ast.MethodDecl;
import polyglot.ast.Call;
import polyglot.ast.Cast;
import polyglot.ast.Special;

import polyglot.util.CodeWriter;
import polyglot.util.UniqueID;
import polyglot.util.Position;

import polyglot.visit.*;
import polyglot.types.*;

import polyglot.ext.jl.ast.FieldDecl_c;

import abc.aspectj.visit.*;
import abc.aspectj.types.AspectJTypeSystem;
import abc.aspectj.types.AJContext;
import abc.aspectj.types.InterTypeFieldInstance_c;

import abc.weaving.aspectinfo.MethodCategory;

public class IntertypeFieldDecl_c extends FieldDecl_c
    implements IntertypeFieldDecl, ContainsAspectInfo
{
    protected TypeNode host;
    protected InterTypeFieldInstance_c hostInstance;
    protected LocalInstance thisParamInstance;
    protected Supers supers;

    protected MethodDecl initm;

    public IntertypeFieldDecl_c(Position pos,
                                Flags flags,
                                TypeNode type,
                                TypeNode host,
                                String name,
                                Expr init){
	super(pos,flags,type,name,init);
	this.host = host;
	this.supers = new Supers();
    }
    
    public TypeNode host() { 
    	return host;
    }

    protected IntertypeFieldDecl_c reconstruct(TypeNode type, 
					       Expr init,
					       TypeNode host) {
	if(host != this.host) {
	    IntertypeFieldDecl_c n = (IntertypeFieldDecl_c) copy();
	    n.host=host;
	    return (IntertypeFieldDecl_c) n.reconstruct(type,init);
	}
	return (IntertypeFieldDecl_c) super.reconstruct(type,init);
    }

    public Node visitChildren(NodeVisitor v) {
		TypeNode type = (TypeNode) visitChild(type(), v);
        Expr init = (Expr) visitChild(init(), v);
		TypeNode host=(TypeNode) visitChild(this.host,v);
		return reconstruct(type,init,host);
    }
    
    public Node typeCheck(TypeChecker tc) throws SemanticException {
    	if (flags().isProtected())
    		throw new SemanticException("Intertype fields cannot be protected",position());
    	if (flags().isStatic() && host.type().toClass().flags().isInterface())
    		throw new SemanticException("Intertype fields on interfaces cannot be static");
    	return super.typeCheck(tc);
    }

	/**
	 * @author Oege de Moor
	 * @author Aske Christensen
	 * add intertype field declarations to host types
	 */
    public NodeVisitor addMembersEnter(AddMemberVisitor am) {
		Type ht = host.type();
		if (ht instanceof ParsedClassType) {
			// need to make a copy because the container has changed
			AspectJTypeSystem ts = (AspectJTypeSystem) am.typeSystem();
			
			InterTypeFieldInstance_c fi = 
			  (InterTypeFieldInstance_c)
			  ts.interTypeFieldInstance(position(),(ClassType) fieldInstance().container(), // origin
			                            (ReferenceType) ht, 
			                            fieldInstance().flags(),
										fieldInstance().type(),
										fieldInstance().name());
	   	 	((ParsedClassType)ht).addField(fi); // add field for type checking
	   	 	
	   	 	hostInstance = fi;
	   	 	
	   	 	
	   	 	
			/* record instance for "this" parameter */
			String name = UniqueID.newID("this");
			thisParamInstance = ts.localInstance(position,Flags.NONE,host.type(),name);
		}
        return am.bypassChildren(this);
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
	
	protected MethodInstance initmi;
	
	/** 
	 * create a new method for the initialiser, that has "this" of host type as 
	 * a parameter. TODO: If it is static, however, it does not have any parameters.
	 * @author Oege de Moor
	 */
	public MethodDecl initMethod(AspectJNodeFactory nf, AspectJTypeSystem ts) {
		String name = UniqueID.newID("init$"+name());
		
		List formals = new LinkedList();
		
		if (!(flags().isStatic())) {
		// the "this" parameter:
			TypeNode tn = nf.CanonicalTypeNode(position(),thisParamInstance.type());
			Formal thisParam = nf.Formal(position(),Flags.FINAL,tn,thisParamInstance.name());
			thisParam = thisParam.localInstance(thisParamInstance);
			formals.add(thisParam);
		}
		
		List throwTypes = new LinkedList();
		for (Iterator i = init().throwTypes(ts).iterator(); i.hasNext(); ) {
			Type t = (Type) i.next();
			TypeNode ttn = nf.CanonicalTypeNode(position(),t);
			throwTypes.add(ttn);
		}
		
		Cast cast = nf.Cast(position(),type(),init());
		cast = (Cast) cast.type(type().type());
		
		Return ret = nf.Return(init().position(),cast);
		Block body = nf.Block(init().position(),ret);
		
		TypeNode rettype = nf.CanonicalTypeNode(position(),type().type());
		
		Flags fs = Flags.PUBLIC.set(Flags.STATIC);
		
		MethodDecl md = nf.MethodDecl(position(),fs,rettype,name,formals,throwTypes,body);
		
		List argtypes = new LinkedList();
		if (!(flags().isStatic()))
			argtypes.add(thisParamInstance.type());
		List exctypes = init().throwTypes(ts);
		initmi = ts.methodInstance(position(),fieldInstance().container(),fs,type().type(),name,argtypes,exctypes);
		md = md.methodInstance(initmi);

		initm = md;
		return md;
	}
	
	/** replace init by method call. Note: this methodcall occurs in the host,
	 * not in the originating aspect.
	 * @author Oege de Moor
	 */
	public IntertypeFieldDecl liftInit(AspectJNodeFactory nf, AspectJTypeSystem ts) {
		List args = new LinkedList(); 
		if (!(flags().isStatic())) {
			Special targetThisRef = nf.Special(position(),Special.THIS,host);
			targetThisRef = (Special) targetThisRef.type(host.type());
			args.add(targetThisRef);
		}
		Call c = nf.Call(position,host,initmi.name(),args);
		c = c.methodInstance(initmi);
		
		return (IntertypeFieldDecl) init(c);
	}
	
	/** retrieve the supers */
	public Supers getSupers() {
		return supers;
	}
	
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
		w.write(flags().translate());
        print(type(), w, tr);
        w.write(" ");
        print(host, w, tr);
        w.write(".");
        w.write(name());

        if (init() != null) {
            w.write(" =");
            w.allowBreak(2, " ");
            print(init(), w, tr);
        }

        w.write(";");
    }
    
    /**
     * @author Oege de Moor
     * change private intertype field decl into public,
     * mangling the name.
     */
    public IntertypeFieldDecl accessChange() {
    	if (flags().isPrivate() || flags().isPackage()) {
    		ParsedClassType ht = (ParsedClassType) host.type();
    		InterTypeFieldInstance_c fi = (InterTypeFieldInstance_c) ht.fieldNamed(name());
    		ht.fields().remove(fi); // remove old instance from host type    		
    		FieldInstance mi = fi.mangled();  // retrieve the mangled instance 		
    		ht.addField(mi); // add new instance to host type   		
    		return (IntertypeFieldDecl) name(mi.name()).fieldInstance(mi).flags(mi.flags());
    	}
    	return this;
    }
    
   
	
	/**
	* @author Oege de Moor
	* record the host type in the environment, for checking of this and super.
	* also add fields and methods of the host that are visible from the aspect.
	*/
	
	public Context enterScope(Node n, Context c) {
		AJContext nc = (AJContext) super.enterScope(c);
		if (n==init()) {
			TypeSystem ts = nc.typeSystem();
			AJContext ncc = (AJContext) nc.pushHost(ts.staticTarget(host.type()).toClass(),
											 flags().isStatic());
			ncc.addITMembers(host.type().toClass());
			return ncc;
		} else return nc;
	}
	
    public void update(abc.weaving.aspectinfo.GlobalAspectInfo gai, abc.weaving.aspectinfo.Aspect current_aspect) {
    	if (init() != null)
			MethodCategory.register(initm, MethodCategory.INTERTYPE_FIELD_INITIALIZER);

		// System.out.println("IFD host: "+host.toString());
		abc.weaving.aspectinfo.FieldSig fs = new abc.weaving.aspectinfo.FieldSig
	  			  	(AspectInfoHarvester.convertModifiers(flags()),
	   				gai.getClass(host.toString()),
	     			AspectInfoHarvester.toAbcType(type().type()),
	     			name(),
	     			null);
	    Call c = (Call) init();
		abc.weaving.aspectinfo.MethodSig initSig;
	    if (c != null) {
	    	MethodInstance mi = c.methodInstance();
			List formals = new LinkedList(); 
			Iterator fi = mi.formalTypes().iterator(); int i = 0;
			while (fi.hasNext()) {
				Type f = (Type)fi.next();
				formals.add(new abc.weaving.aspectinfo.Formal(AspectInfoHarvester.toAbcType(f),
						   "a"+i, position()));
				i++;
			}
			List exc = new LinkedList();
			Iterator ti = mi.throwTypes().iterator();
			while (ti.hasNext()) {
				Type t = (Type)ti.next();
				exc.add(t.toString());
			}
			initSig = new abc.weaving.aspectinfo.MethodSig
				(AspectInfoHarvester.convertModifiers(mi.flags()),
				 current_aspect.getInstanceClass(),
				 AspectInfoHarvester.toAbcType(mi.returnType()),
				 mi.name(),
				 formals,
				 exc,
				 position());}
		else initSig = null;
		abc.weaving.aspectinfo.IntertypeFieldDecl ifd = new abc.weaving.aspectinfo.IntertypeFieldDecl
	    			(fs, current_aspect, initSig, AspectInfoHarvester.convertSig(hostInstance.getGet()),
	    				AspectInfoHarvester.convertSig(hostInstance.getSet()),  position());
		gai.addIntertypeFieldDecl(ifd);
		gai.addSuperDispatches(supers.supercalls(gai));
		gai.addSuperFieldGetters(supers.superfieldgetters(gai));
		gai.addSuperFieldSetters(supers.superfieldsetters(gai));
    }
}
