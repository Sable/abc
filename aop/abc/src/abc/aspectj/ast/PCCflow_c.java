package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

import abc.aspectj.types.AJContext;

public class PCCflow_c extends Pointcut_c implements PCCflow
{
    protected Pointcut pc;
    protected int depth=-1;

    public PCCflow_c(Position pos, Pointcut pc)  {
		super(pos);
        this.pc = pc;
    }

    public Precedence precedence() {
		return Precedence.LITERAL;
    }
    
	public Set pcRefs() {
		return pc.pcRefs();
	}
	

    public void recordCflowDepth(int depth) {
	this.depth=depth;
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

	public Node typeCheck(TypeChecker tc) throws SemanticException {
			AJContext c = (AJContext) tc.context();
			if (c.inDeclare())
				throw new SemanticException("cflow(..) requires a dynamic test and cannot be used inside a \"declare\" statement", position());
			return this;
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

    public abc.weaving.aspectinfo.Pointcut makeAIPointcut() {
	if(depth==-1) throw new InternalCompilerError
			  ("Depth of cflow should have been recorded by now");
	return new abc.weaving.aspectinfo.Cflow
	    (pc.makeAIPointcut(),position(),depth);
    }
}
