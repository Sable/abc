package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class PerTarget_c extends PerClause_c implements PerTarget
{

    Pointcut pc;

    public PerTarget_c(Position pos, Pointcut pc)
    {
	super(pos);
        this.pc = pc;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("pertarget (");
        print(pc, w, tr);
        w.write(")");
    }

    public abc.weaving.aspectinfo.Per makeAIPer() {
	return new abc.weaving.aspectinfo.PerTarget(pc.makeAIPointcut(),position());
    }
}
