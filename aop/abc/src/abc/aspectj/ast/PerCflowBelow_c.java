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

	protected PerCflowBelow_c reconstruct(Pointcut pc) {
		if (pc != this.pc ) {
			PerCflowBelow_c n = (PerCflowBelow_c) copy();
			n.pc = pc;
			return n;
		}
		return this;
	}


	public Node visitChildren(NodeVisitor v) {
		Pointcut pc = (Pointcut) visitChild(this.pc, v);
		return reconstruct(pc);
	}
	
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("percflowbelow (");
        print(pc, w, tr);
        w.write(")");
    }

    public abc.weaving.aspectinfo.Per makeAIPer() {
	return new abc.weaving.aspectinfo.PerCflowBelow(pc.makeAIPointcut(),position());
    }
}
