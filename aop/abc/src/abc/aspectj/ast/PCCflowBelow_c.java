package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;
import abc.aspectj.types.AJContext;

public class PCCflowBelow_c extends Pointcut_c implements PCCflowBelow
{
    protected Pointcut pc;
    protected int depth=-1;

    public PCCflowBelow_c(Position pos, Pointcut pc)  {
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
    
	public Node typeCheck(TypeChecker tc) throws SemanticException {
			AJContext c = (AJContext) tc.context();
			if (c.inDeclare())
				throw new SemanticException("cflowbelow(..) requires a dynamic test and cannot be used inside a \"declare\" statement", position());
			return this;
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
	return new abc.weaving.aspectinfo.CflowBelow
	    (pc.makeAIPointcut(),position(),depth);
    }
}
