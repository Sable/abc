package arc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class PCNot_c extends Pointcut_c implements PCNot
{
    protected Pointcut pc;

    public PCNot_c(Position pos, Pointcut pc)  {
	super(pos);
        this.pc = pc;
    }

    public Precedence precedence() {
	return Precedence.UNARY;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
		w.write("!");
        printSubExpr(pc, true, w, tr);
    }
    
	/** Reconstruct the pointcut. */
	protected PCNot_c reconstruct(Pointcut pc) {
		 if (pc != this.pc) {
			   PCNot_c n = (PCNot_c) copy();
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


}
