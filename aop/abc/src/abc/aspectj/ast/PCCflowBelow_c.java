package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class PCCflowBelow_c extends Pointcut_c implements PCCflowBelow
{
    protected Pointcut pc;

    public PCCflowBelow_c(Position pos, Pointcut pc)  {
	super(pos);
        this.pc = pc;
    }

    public Precedence precedence() {
	return Precedence.LITERAL;
    }

    /** Reconstruct the pointcut. */
    protected PCCflowBelow_c reconstruct(Pointcut pc) {
	if (pc != this.pc) {
	    PCCflowBelow_c n = (PCCflowBelow_c) copy();
	    n.pc = pc;
	    return n;
	}
	return this;
    }

    /** Visit the children of the pointcut. */
    public Node visitChildren(NodeVisitor v) {
	Pointcut pc = (Pointcut) visitChild(this.pc, v);
	return reconstruct(pc);
    }


    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("cflowbelow(");
        print(pc, w, tr);
        w.write(")");
    }
    
	public Collection mayBind() throws SemanticException {
		return pc.mayBind();
	}
   
	public Collection mustBind() {
		return pc.mustBind();
	}

    public abc.weaving.aspectinfo.Pointcut makeAIPointcut() {
	return new abc.weaving.aspectinfo.ConditionPointcut
	    (new abc.weaving.aspectinfo.CflowBelow(pc.makeAIPointcut()),
	     position());
    }
}
