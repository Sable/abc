package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class DeclareSoft_c extends DeclareDecl_c 
                           implements DeclareSoft
{

    TypeNode type;
    Pointcut pc;

    public DeclareSoft_c(Position pos, 
                         TypeNode type,
                         Pointcut pc)
    {
	super(pos);
        this.type = type;
        this.pc   = pc;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("declare soft : ");
        print(type,w,tr);
        w.write(" : ");
        print(pc, w, tr);
        w.write(";");
    }

}
