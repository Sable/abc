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

public class IntertypeFieldDecl_c extends FieldDecl_c
    implements IntertypeFieldDecl, ContainsAspectInfo
{
    protected TypeNode host;
    protected FieldInstance hostInstance;


    public IntertypeFieldDecl_c(Position pos,
                                Flags flags,
                                TypeNode type,
                                TypeNode host,
                                String name,
                                Expr init){
	super(pos,flags,type,name,init);
	this.host = host;
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
	   	 	
	   	 	
		}
        return am.bypassChildren(this);
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
	 * record the host type in the environment, for checking of this and super
	*/
	public Context enterScope(Context c) {
			AJContext nc = (AJContext) super.enterScope(c);
			TypeSystem ts = nc.typeSystem();
			return nc.pushHost(ts.staticTarget(host.type()).toClass());
	}
	
    public void update(abc.weaving.aspectinfo.GlobalAspectInfo gai, abc.weaving.aspectinfo.Aspect current_aspect) {
		System.out.println("IFD host: "+host.toString());
		abc.weaving.aspectinfo.FieldSig fs = new abc.weaving.aspectinfo.FieldSig
	  			  	(AspectInfoHarvester.convertModifiers(flags()),
	   				gai.getClass(host.toString()),
	     			AspectInfoHarvester.toAbcType(type().type()),
	     			name(),
	     			null);
		abc.weaving.aspectinfo.IntertypeFieldDecl ifd = new abc.weaving.aspectinfo.IntertypeFieldDecl
	    			(fs, current_aspect, position());
		gai.addIntertypeFieldDecl(ifd);
    }
}
