package polyglot.ext.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class PerCflow_c extends PerClause_c implements PerCflow
{

    Pointcut pc;

    public PerCflow_c(Position pos, Pointcut pc)
    {
	super(pos);
        this.pc = pc;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("percflow (");
        print(pc, w, tr);
        w.write(")");
    }

}
