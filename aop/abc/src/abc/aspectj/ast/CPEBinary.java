package abc.aspectj.ast;

import polyglot.util.Enum;
import polyglot.ast.Precedence;


/** binary operators on classname pattern expressions.
 * 
 * @author Oege de Moor
 */
public interface CPEBinary extends ClassnamePatternExpr
{

    public Operator getOperator();
    public ClassnamePatternExpr getLeft();
    public ClassnamePatternExpr getRight();

    public static class Operator extends Enum {
	Precedence prec;

        public Operator(String name, Precedence prec) {
	    super(name);
	    this.prec = prec;
	}

	/** Returns the precedence of the operator. */
	public Precedence precedence() { return prec; }
    }

    public static final Operator COND_OR  = new Operator("||", Precedence.COND_OR);
    public static final Operator COND_AND = new Operator("&&", Precedence.COND_AND);
}
