package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;


public class PerCflowBelow_c extends PerClause_c implements PerCflowBelow
{

    Pointcut pc;

    public PerCflowBelow_c(Position pos, Pointcut pc)
    {
	super(pos);
        this.pc = pc;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("percflowbelow (");
        print(pc, w, tr);
        w.write(")");
    }

}
