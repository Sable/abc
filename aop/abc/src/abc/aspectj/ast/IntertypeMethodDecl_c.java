package abc.aspectj.ast;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import polyglot.ast.Block;
import polyglot.ast.TypeNode;
import polyglot.ast.Formal;
import polyglot.util.CodeWriter;
import polyglot.util.UniqueID;
import polyglot.util.Position;

import polyglot.visit.*;
import polyglot.types.*;

import polyglot.ext.jl.ast.MethodDecl_c;

import abc.aspectj.visit.*;

public class IntertypeMethodDecl_c extends MethodDecl_c
    implements IntertypeMethodDecl, ContainsAspectInfo
{
    protected TypeNode host;

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
    }

    public NodeVisitor addMembersEnter(AddMemberVisitor am) {
	Type ht = host.type();
	if (ht instanceof ParsedClassType) {
	    ((ParsedClassType)ht).addMethod(methodInstance());
	}
        return am.bypassChildren(this);
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
	System.out.println("IMD host: "+host.toString());
	List params = new ArrayList();
	Iterator fi = formals().iterator();
	while (fi.hasNext()) {
	    Formal f = (Formal)fi.next();
	    params.add(AspectInfoHarvester.toAbcType(f.type().type()));
	}
	abc.weaving.aspectinfo.MethodSig impl = new abc.weaving.aspectinfo.MethodSig
	    (current_aspect.getInstanceClass(),
	     AspectInfoHarvester.toAbcType(returnType().type()),
	     name(),
	     params,
	     position());
	abc.weaving.aspectinfo.MethodSig target = new abc.weaving.aspectinfo.MethodSig
	    (gai.getClass(host.toString()),
	     AspectInfoHarvester.toAbcType(returnType().type()),
	     name(),
	     params,
	     null);
	abc.weaving.aspectinfo.IntertypeMethodDecl imd = new abc.weaving.aspectinfo.IntertypeMethodDecl
	    (target, impl, current_aspect, position());
	gai.addIntertypeMethodDecl(imd);
    }
}
	

	

     


