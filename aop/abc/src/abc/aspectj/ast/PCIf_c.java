package arc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class PCIf_c extends Pointcut_c implements PCIf
{
    protected Expr expr;

    public PCIf_c(Position pos, Expr expr)  {
	super(pos);
        this.expr = expr;
    }

    public Precedence precedence() {
		return Precedence.LITERAL;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
		w.write("if(");
        print(expr, w, tr);
        w.write(")");
    }
    
	/** Reconstruct the pointcut. */
	protected PCIf_c reconstruct(Expr expr) {
	   if (expr != this.expr) {
		   PCIf_c n = (PCIf_c) copy();
		   n.expr = expr;
		   return n;
	   }

	   return this;
	}

	/** Visit the children of the pointcut. */
	public Node visitChildren(NodeVisitor v) {
	   Expr expr = (Expr) visitChild(this.expr, v);
	   return reconstruct(expr);
	}

    
	/** Type check the pointcut. */
	public Node typeCheck(TypeChecker tc) throws SemanticException {
		TypeSystem ts = tc.typeSystem();

		if (! ts.equals(expr.type(), ts.Boolean())) {
			throw new SemanticException(
			"Condition of if pointcut must have boolean type.",
			expr.position());
		}
		
		return this;
	}

	public Type childExpectedType(Expr child, AscriptionVisitor av) {
		TypeSystem ts = av.typeSystem();

		if (child == expr) {
			return ts.Boolean();
		}

		return child.type();
	}


}
