package abc.aspectj.ast;

import java.util.Iterator;
import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.TypeNode;
import polyglot.ast.Formal;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.util.CodeWriter;
import polyglot.util.UniqueID;
import polyglot.util.Position;

import polyglot.visit.*;
import polyglot.types.*;

import polyglot.ext.jl.ast.FieldDecl_c;

import abc.aspectj.visit.*;

public class IntertypeFieldDecl_c extends FieldDecl_c
    implements IntertypeFieldDecl, ContainsAspectInfo
{
    protected TypeNode host;

    public IntertypeFieldDecl_c(Position pos,
                                Flags flags,
                                TypeNode type,
                                TypeNode host,
                                String name,
                                Expr init){
	super(pos,flags,type,name,init);
	this.host = host;
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

    public NodeVisitor addMembersEnter(AddMemberVisitor am) {
	Type ht = host.type();
	if (ht instanceof ParsedClassType) {
	    ((ParsedClassType)ht).addField(fieldInstance());
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

    public void update(abc.weaving.aspectinfo.GlobalAspectInfo gai, abc.weaving.aspectinfo.Aspect current_aspect) {
	System.out.println("IFD host: "+host.toString());
	abc.weaving.aspectinfo.FieldSig fs = new abc.weaving.aspectinfo.FieldSig
	    (gai.getClass(host.toString()),
	     AspectInfoHarvester.toAbcType(type().type()),
	     name(),
	     null);
	abc.weaving.aspectinfo.IntertypeFieldDecl ifd = new abc.weaving.aspectinfo.IntertypeFieldDecl
	    (fs, current_aspect, position());
	gai.addIntertypeFieldDecl(ifd);
    }
}
