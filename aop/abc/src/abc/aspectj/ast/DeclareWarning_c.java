package arc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class DeclareWarning_c extends DeclareDecl_c 
                              implements DeclareWarning
{

    Pointcut pc;
    String text;

    public DeclareWarning_c(Position pos, 
                            Pointcut pc,
                            String text)
    {
	super(pos);
        this.pc   = pc;
        this.text = text;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("declare warning : ");
        print(pc, w, tr);
        w.write(" : ");
        w.write("\""+text+"\"");
        w.write(";");
    }

}
