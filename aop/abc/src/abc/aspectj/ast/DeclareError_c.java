package polyglot.ext.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class DeclareError_c extends DeclareDecl_c 
                            implements DeclareError
{

    Pointcut pc;
    String text;

    public DeclareError_c(Position pos, 
                          Pointcut pc,
                          String text)
    {
	super(pos);
        this.pc   = pc;
        this.text = text;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("declare error : ");
        print(pc, w, tr);
        w.write(" : ");
        w.write("\""+text+"\"");
        w.write(";");
    }

}
