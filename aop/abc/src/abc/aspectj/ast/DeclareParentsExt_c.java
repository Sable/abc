package arc.aspectj.ast;

import arc.aspectj.visit.*;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;

import polyglot.ext.jl.ast.*;

import java.util.*;

public class DeclareParentsExt_c extends DeclareDecl_c 
                                 implements DeclareParentsExt
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

    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
	//System.out.println("DeclareParentsExt 0 "+ar.kind());
	if (ar.kind() == DeclareParentsAmbiguityRemover.DECLARE) {
	    //System.out.println("DeclareParentsExt 1 "+((AmbTypeNode)type).name());
	    type = (TypeNode)type.disambiguate(ar);
	    //System.out.println("DeclareParentsExt 2 "+type.type());
	}

	return this;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("declare parents : ");
        print(pat, w, tr);
        w.write(" extends ");
        print(type,w,tr);
        w.write(";");
    }

}
