package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

import abc.aspectj.types.AspectType;

/**
 * 
 * @author Oege de Moor
 *
 */
public class PerCflow_c extends PerClause_c implements PerCflow
{

    Pointcut pc;

    public PerCflow_c(Position pos, Pointcut pc)
    {
	super(pos);
        this.pc = pc;
    }

	protected PerCflow_c reconstruct(Pointcut pc) {
		if (pc != this.pc ) {
			PerCflow_c n = (PerCflow_c) copy();
			n.pc = pc;
			return n;
		}
		return this;
	}

	public int kind() {
		return AspectType.PER_CFLOW;
	}
	
	public Node visitChildren(NodeVisitor v) {
		Pointcut pc = (Pointcut) visitChild(this.pc, v);
		return reconstruct(pc);
	}
	
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("percflow (");
        print(pc, w, tr);
        w.write(")");
    }

    public abc.weaving.aspectinfo.Per makeAIPer() {
	return new abc.weaving.aspectinfo.PerCflow(pc.makeAIPointcut(),position());
    }

}
