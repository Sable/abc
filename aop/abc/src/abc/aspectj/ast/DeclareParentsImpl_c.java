package arc.aspectj.ast;

import arc.aspectj.visit.*;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class DeclareParentsImpl_c extends DeclareDecl_c 
                                 implements DeclareParentsImpl
{

    ClassnamePatternExpr pat;
    TypedList interfaces;

    public DeclareParentsImpl_c(Position pos, 
                               ClassnamePatternExpr pat,
                               List interfaces)
    {
	super(pos);
        this.pat  = pat;
        this.interfaces = TypedList.copyAndCheck(interfaces,
                                                 TypeNode.class,
                                                 true);
    }

    protected DeclareParentsImpl_c reconstruct(ClassnamePatternExpr pat,
					       TypedList interfaces) {
	if (pat != this.pat || !CollectionUtil.equals(interfaces, this.interfaces)) {
	    DeclareParentsImpl_c n = (DeclareParentsImpl_c) copy();
	    n.pat = pat;
	    n.interfaces = TypedList.copyAndCheck(interfaces, TypeNode.class, true);
	    return n;
	}
	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	ClassnamePatternExpr pat = (ClassnamePatternExpr) visitChild(this.pat, v);
	TypedList interfaces = (TypedList) visitList(this.interfaces, v);
	return reconstruct(pat, interfaces);
    }

    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
	//System.out.println("DeclareParentsImpl 0 "+ar.kind());
	if (ar.kind() == DeclareParentsAmbiguityRemover.DECLARE) {
	    TypedList interfaces_disam = new TypedList(new ArrayList(), TypeNode.class, false);
	    Iterator ti = interfaces.iterator();
	    while (ti.hasNext()) {
		TypeNode tn = (TypeNode)ti.next();
		//System.out.println("DeclareParentsImpl 1 "+((AmbTypeNode)tn).name());
		tn = (TypeNode)tn.disambiguate(ar);
		//System.out.println("DeclareParentsImpl 2 "+tn.type());
		interfaces_disam.add(tn);
	    }
	    interfaces = interfaces_disam;
	}

	return this;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("declare parents : ");
        print(pat, w, tr);
        w.write(" implements ");
        for (Iterator i = interfaces.iterator(); i.hasNext(); ) {
           TypeNode tn = (TypeNode) i.next();
           print(tn, w, tr);

           if (i.hasNext()) {
                w.write (", ");
           }
        }
        w.write(";");
    }

}
