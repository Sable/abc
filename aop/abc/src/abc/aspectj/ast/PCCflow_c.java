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
    
	/** Reconstruct the pointcut. */
	protected PCCflow_c reconstruct(Pointcut pc) {
		if (pc != this.pc) {
			PCCflow_c n = (PCCflow_c) copy();
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
		w.write("cflow(");
        print(pc, w, tr);
        w.write(")");
    }


	public Collection mayBind() throws SemanticException {
		return pc.mayBind();
	}
   
	public Collection mustBind() {
	 	return pc.mustBind();
	}

}
