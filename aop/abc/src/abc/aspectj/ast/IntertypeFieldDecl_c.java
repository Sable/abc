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
import polyglot.ast.ArrayInit;

import polyglot.util.CodeWriter;
import polyglot.util.UniqueID;
import polyglot.util.Position;

import polyglot.visit.*;
import polyglot.types.*;

import polyglot.ext.jl.ast.FieldDecl_c;

import abc.aspectj.ast.AJNodeFactory;
import abc.aspectj.visit.*;
import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.types.AJContext;
import abc.aspectj.types.InterTypeFieldInstance;
import abc.aspectj.types.InterTypeFieldInstance_c;

import abc.weaving.aspectinfo.AbcFactory;
import abc.weaving.aspectinfo.MethodCategory;
import abc.weaving.aspectinfo.GlobalAspectInfo;

/**
 * 
 *  @author Oege de Moor
 *  @author Aske Simon Christensen
 *
 */
public class IntertypeFieldDecl_c extends FieldDecl_c
    implements IntertypeFieldDecl, ContainsAspectInfo, MakesAspectMethods
{
    protected TypeNode host;
    protected InterTypeFieldInstance hostInstance;
    protected LocalInstance thisParamInstance;
    protected String identifier;
    protected String originalName;
    protected Flags originalFlags;

    protected MethodDecl initm;

    public IntertypeFieldDecl_c(Position pos,
                                Flags flags,
                                TypeNode type,
                                TypeNode host,
                                String name,
                                Expr init){
	super(pos,flags,type,name,init);
	this.host = host;
	this.identifier = UniqueID.newID("id");
	this.originalName = name;
	this.originalFlags = flags;
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
	if (host.type() instanceof ParsedClassType &&
	    !abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getWeavableClasses()
	    .contains(abc.weaving.aspectinfo.AbcFactory.AbcClass((ParsedClassType) host.type())))
	    throw new SemanticException("Host of an intertype declaration must be a weavable class");
	    
    	return super.typeCheck(tc);
    }

	/**
	 * @author Oege de Moor
	 * @author Aske Simon Christensen
	 * add intertype field declarations to host types
	 */
    public NodeVisitor addMembersEnter(AddMemberVisitor am) {
		Type ht = host.type();
		if (ht instanceof ParsedClassType) {
			// need to make a copy because the container has changed
			AJTypeSystem ts = (AJTypeSystem) am.typeSystem();
			
			// System.out.println("add field "+name() + " to "+ ht + " from " + fieldInstance().container());
			InterTypeFieldInstance_c fi = 
			  (InterTypeFieldInstance_c)
			  ts.interTypeFieldInstance(position(),identifier,(ClassType) fieldInstance().container(), // origin
			                            (ReferenceType) ht, 
			                            fieldInstance().flags(),
										fieldInstance().type(),
										fieldInstance().name());
			overrideITDField((ParsedClassType)ht,fi);
	   	 	// ((ParsedClassType)ht).addField(fi); // add field for type checking
	   	 	
	   	 	hostInstance = fi;
	   	 	
	   	 	
	   	 	
			/* record instance for "this" parameter */
			String name = UniqueID.newID("this");
			thisParamInstance = ts.localInstance(position,Flags.NONE,host.type(),name);
		} 
        return am.bypassChildren(this);
    }

	static List fieldsNamed(ClassType ct, String name) {
		List result = new LinkedList();
		for (Iterator fldit = ct.fields().iterator(); fldit.hasNext(); ) {
			FieldInstance fi = (FieldInstance) fldit.next();
			if (fi.name().equals(name))
				result.add(fi);
		}
		return result;
	}
	
	public static void overrideITDField(ClassType pht, FieldInstance fi) {
		FieldInstance toInsert = fi; //.container(pht);
		boolean added = false;
		if (pht.fieldNamed(fi.name())!= null) {
			List fis = fieldsNamed(pht,fi.name());
			
			for (Iterator fisIt = fis.iterator(); fisIt.hasNext(); ) {
				FieldInstance finst = (FieldInstance) fisIt.next();
				if (zaps(fi,finst) && !added){   
					pht.fields().remove(finst);
					pht.fields().add(toInsert);
					added = true;
				}
				else if (zaps(finst,fi)) {	
					// skip  
					}
				else if (!added) { pht.fields().add(toInsert); added = true; } 
			}
		} else  {pht.fields().add(toInsert); added=true;}
		if (added)
			abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().registerWeave(AbcFactory.AbcClass(pht));
	}
	
	// replace this by a call to the appropriate structure!
	static boolean precedes(ClassType ct1, ClassType ct2) {
		return ct1.descendsFrom(ct2);
	}

	static boolean zaps(FieldInstance mi1,FieldInstance mi2) {
		if (!(mi1 instanceof InterTypeFieldInstance_c &&
			  mi2 instanceof InterTypeFieldInstance_c)) return false;
		InterTypeFieldInstance itmi1 = (InterTypeFieldInstance) mi1;
		InterTypeFieldInstance itmi2 = (InterTypeFieldInstance) mi2;
		return precedes(itmi1.origin(),itmi2.origin());	    
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
	
	protected MethodInstance initmi;
	
	/** 
	 * create a new method for the initialiser, that has "this" of host type as 
	 * a parameter. TODO: If it is static, however, it does not have any parameters.
	 * @author Oege de Moor
	 */
	public MethodDecl initMethod(AJNodeFactory nf, AJTypeSystem ts) {
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
		for (Iterator i = init().del().throwTypes(ts).iterator(); i.hasNext(); ) {
			Type t = (Type) i.next();
			TypeNode ttn = nf.CanonicalTypeNode(position(),t);
			throwTypes.add(ttn);
		}
		
		Expr initExpr = init();
		if (init() instanceof ArrayInit) {
			ArrayType initType = (ArrayType) init().type();
			initExpr = nf.NewArray(position,type(),initType.dims(),(ArrayInit)init()).type(initType);
		}
		
		Cast cast = nf.Cast(position(),type(),initExpr);
		cast = (Cast) cast.type(type().type());
		
		Return ret = nf.Return(init().position(),cast);
		Block body = nf.Block(init().position(),ret);
		
		TypeNode rettype = nf.CanonicalTypeNode(position(),type().type());
		
		Flags fs = Flags.PUBLIC.set(Flags.STATIC);
		
		MethodDecl md = nf.MethodDecl(position(),fs,rettype,name,formals,throwTypes,body);
		
		List argtypes = new LinkedList();
		if (!(flags().isStatic()))
			argtypes.add(thisParamInstance.type());
		List exctypes = init().del().throwTypes(ts);
		initmi = ts.methodInstance(position(),fieldInstance().container(),fs,type().type(),name,argtypes,exctypes);
		md = md.methodInstance(initmi);

		initm = md;
		return md;
	}
	
	/** replace init by method call. Note: this methodcall occurs in the host,
	 * not in the originating aspect.
	 * @author Oege de Moor
	 */
	public IntertypeFieldDecl liftInit(AJNodeFactory nf, AJTypeSystem ts) {
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
    	if (flags().isPrivate() || flags().isPackage() || host.type().toClass().flags().isInterface()) {
    		ParsedClassType ht = (ParsedClassType) host.type();
    		InterTypeFieldInstance fi = hostInstance; // was findFieldNamed...
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
			return ncc.addITMembers(host.type().toClass());
		} else return nc;
	}
	
    public void update(abc.weaving.aspectinfo.GlobalAspectInfo gai, abc.weaving.aspectinfo.Aspect current_aspect) {
    	if (init() != null)
			MethodCategory.register(initm, MethodCategory.INTERTYPE_FIELD_INITIALIZER);

		// System.out.println("IFD host: "+host.toString());
		abc.weaving.aspectinfo.FieldSig fs = new abc.weaving.aspectinfo.FieldSig
	  			  	(AbcFactory.modifiers(flags()),
	   				AbcFactory.AbcClass((ClassType)host.type()),
	     			AbcFactory.AbcType(type().type()),
	     			name(),
	     			null);
		gai.registerRealNameAndClass(fs,
					AbcFactory.modifiers(originalFlags),
					originalName,
					AbcFactory.AbcClass((ClassType)host.type()));
					
	    Call c = (Call) init();
		abc.weaving.aspectinfo.MethodSig initSig;
	    if (c != null) {
			MethodInstance mi = c.methodInstance();
			initSig = AbcFactory.MethodSig(mi);
		}
		else initSig = null;
		MethodInstance get = hostInstance.getGet();
		MethodInstance set = hostInstance.getSet();
		abc.weaving.aspectinfo.MethodSig getsig = get == null ? null : AbcFactory.MethodSig(get);
		abc.weaving.aspectinfo.MethodSig setsig = set == null ? null : AbcFactory.MethodSig(set);
		abc.weaving.aspectinfo.IntertypeFieldDecl ifd = new abc.weaving.aspectinfo.IntertypeFieldDecl
		    (fs, current_aspect, initSig, getsig, setsig, position());
		gai.addIntertypeFieldDecl(ifd);
		
    }

    public void aspectMethodsEnter(AspectMethods visitor)
    {
        visitor.pushIntertypeDecl(this);
    }

    public Node aspectMethodsLeave(AspectMethods visitor, AJNodeFactory nf,
                                   AJTypeSystem ts)
    {
        IntertypeFieldDecl_c itfd = this;
        visitor.popIntertypeDecl();

        if (itfd.init() != null) {
            MethodDecl md = itfd.initMethod(nf,ts);
            visitor.addMethod(md);
            itfd = (IntertypeFieldDecl_c) itfd.liftInit(nf,ts);
        }
        return itfd.accessChange(); // mangle name if private
    }
}
