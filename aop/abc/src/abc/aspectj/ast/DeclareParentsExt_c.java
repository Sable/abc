package abc.aspectj.ast;

import abc.aspectj.visit.*;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;

import polyglot.ext.jl.ast.*;

import java.util.*;

import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.GlobalAspectInfo;

import abc.weaving.aspectinfo.AbcFactory;

public class DeclareParentsExt_c extends DeclareDecl_c 
    implements DeclareParentsExt, ContainsAspectInfo
{

    ClassnamePatternExpr pat;
    TypeNode type;

    public DeclareParentsExt_c(Position pos, 
                               ClassnamePatternExpr pat,
                               TypeNode type)
    {
	super(pos);
        this.pat  = pat;
        this.type = type;
    }

    protected DeclareParentsExt_c reconstruct(ClassnamePatternExpr pat,
					      TypeNode type) {
	if (pat != this.pat || type != this.type) {
	    DeclareParentsExt_c n = (DeclareParentsExt_c) copy();
	    n.pat = pat;
	    n.type = type;
	    return n;
	}
	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	ClassnamePatternExpr pat = (ClassnamePatternExpr) visitChild(this.pat, v);
	TypeNode type = (TypeNode) visitChild(this.type, v);
	return reconstruct(pat, type);
    }

    public Node disambiguate(DeclareParentsAmbiguityRemover ar) throws SemanticException {
	type = (TypeNode)ar.disamb(type);
	return this;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("declare parents : ");
        print(pat, w, tr);
        w.write(" extends ");
        print(type,w,tr);
        w.write(";");
    }

    public ClassnamePatternExpr pat() {
	return pat;
    }

    public TypeNode type() {
	return type;
    }

    public void update(GlobalAspectInfo gai, Aspect current_aspect) {
	//System.out.println("Declare parents ext");
	gai.addDeclareParents(new abc.weaving.aspectinfo.DeclareParentsExt
			      (pat.makeAIClassnamePattern(),
			       AbcFactory.AbcClass((ClassType)type.type()),
			       current_aspect,
			       position()));
    }
}
