package polyglot.ext.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class DeclareParentsExt_c extends DeclareDecl_c 
                                 implements DeclareParentsExt
{

    ClassnamePatternExpr pat;
    List types;

    public DeclareParentsExt_c(Position pos, 
                               ClassnamePatternExpr pat,
                               List types)
    {
	super(pos);
        this.pat  = pat;
        this.types = TypedList.copyAndCheck(types,TypeNode.class,true);
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("declare parents : ");
        print(pat, w, tr);
        w.write(" extends ");
        for (Iterator i = types.iterator(); i.hasNext(); ) {
	       TypeNode n = (TypeNode) i.next();
                print(n,w,tr);
		
		if (i.hasNext()) {
		    w.write(",");
		    w.allowBreak(4, " ");
		}
	    }
        w.write(";");
    }

}
