package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;

import polyglot.ext.jl.ast.Node_c;

import java.util.*;

public class PCBinary_c extends Pointcut_c implements PCBinary
{
    protected Pointcut left;
    protected Operator op;
    protected Pointcut right;
    protected Precedence precedence;

    public PCBinary_c(Position pos, Pointcut left, Operator op, Pointcut right)    {
		super(pos);
        this.left = left;
		this.op = op;
		this.right = right;
		this.precedence = op.precedence();
    }

    public Precedence precedence() {
        return precedence;
    }
    
	public Set pcRefs() {
		left.pcRefs().addAll(right.pcRefs());
		return left.pcRefs();
	}
	
	public boolean isDynamic() {
		return left.isDynamic() || right.isDynamic();
	}

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
		printSubExpr(left, true, w, tr);
		w.write(" ");
		w.write(op.toString());
		w.allowBreak(2, " ");
		printSubExpr(right, false, w, tr);
    }
    
	protected PCBinary_c reconstruct(Pointcut left,
	                                 Pointcut right) {
		if (left != this.left || op != this.op || right != this.right) {
			PCBinary_c n = (PCBinary_c) copy();
			n.left = left;
			n.right = right;
			return n;
		}

		return this;
	}

	public Node visitChildren(NodeVisitor v) {
		Pointcut left = (Pointcut) visitChild(this.left,v);
		Pointcut right = (Pointcut) visitChild(this.right,v);
		return reconstruct(left,right);
	}

	public Collection mayBind() throws SemanticException {
		Collection result = left.mayBind();
		Collection result2 = right.mayBind();
  		for (Iterator i = result2.iterator(); i.hasNext(); ) {
			String pat = (String) i.next();
		    if (op == PCBinary.COND_AND && result.contains(pat))
			    throw new SemanticException("Repeated binding of \""+ pat +"\".",
																			   position()); // somewhat inaccurate position info
		    else result.add(pat);
		}
		return result;
	}
   
		public Collection mustBind() {
			Collection result = left.mustBind();
			if (op == PCBinary.COND_AND)
				result.addAll(right.mustBind());
			else if (op == PCBinary.COND_OR)
			    result.retainAll(right.mustBind());
			return result;
		}

    public abc.weaving.aspectinfo.Pointcut makeAIPointcut() {
	if (op == PCBinary.COND_AND) {
	    return abc.weaving.aspectinfo.AndPointcut.construct(left.makeAIPointcut(),right.makeAIPointcut(), position());
	} else if (op == PCBinary.COND_OR) {
	    return abc.weaving.aspectinfo.OrPointcut.construct(left.makeAIPointcut(),right.makeAIPointcut(), position());
	} else {
	    throw new InternalCompilerError("Unexpected binary pointcut operation: "+op,position());
	}
    }
}
