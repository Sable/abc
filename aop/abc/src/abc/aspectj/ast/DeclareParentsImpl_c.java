package polyglot.ext.aspectj.ast;

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
