package abc.aspectj.ast;

import polyglot.util.Enum;
import polyglot.ast.Precedence;

public interface TPEBinary extends TypePatternExpr
{

    public static class Operator extends Enum {
	Precedence prec;

        public Operator(String name, Precedence prec) {
	    super(name);
	    this.prec = prec;
	}

	/** Returns the precedence of the operator. */
	public Precedence precedence() { return prec; }
    }

    public TypePatternExpr left();
    public TypePatternExpr right();
    public Operator op();

    public static final Operator COND_OR  = new Operator("||", Precedence.COND_OR);
    public static final Operator COND_AND = new Operator("&&", Precedence.COND_AND);
}
