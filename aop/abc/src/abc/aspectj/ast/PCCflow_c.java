package arc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class PCCflow_c extends Pointcut_c implements PCCflow
{
    protected Pointcut pc;

    public PCCflow_c(Position pos, Pointcut pc)  {
	super(pos);
        this.pc = pc;
    }

    public Precedence precedence() {
	return Precedence.LITERAL;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("cflow(");
        print(pc, w, tr);
        w.write(")");
    }

}
