package arc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
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

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("declare parents : ");
        print(pat, w, tr);
        w.write(" extends ");
        print(type,w,tr);
        w.write(";");
    }

}
