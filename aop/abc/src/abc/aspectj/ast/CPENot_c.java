package abc.aspectj.ast;

import abc.aspectj.visit.*;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

/** negation of a ClassnamePatternExpr.
 * 
 * @author Oege de Moor
 */
public class CPENot_c extends ClassnamePatternExpr_c implements CPENot
{
    protected ClassnamePatternExpr cpe;

    public CPENot_c(Position pos, ClassnamePatternExpr cpe)  {
	super(pos);
        this.cpe = cpe;
    }

    public ClassnamePatternExpr getCpe() {
	return cpe;
    }

    protected CPENot_c reconstruct(ClassnamePatternExpr cpe) {
	if (cpe != this.cpe) {
	    CPENot_c n = (CPENot_c) copy();
	    n.cpe = cpe;
	    return n;
	}
	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	ClassnamePatternExpr cpe = (ClassnamePatternExpr) visitChild(this.cpe, v);
	return reconstruct(cpe);
    }

    public Precedence precedence() {
	return Precedence.UNARY;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("!");
        printSubExpr(cpe, true, w, tr);
    }

    public String toString() {
	return "!"+cpe;
    }

    public boolean matches(PatternMatcher matcher, PCNode cl) {
	return !cpe.matches(matcher, cl);
    }

    public boolean equivalent(ClassnamePatternExpr otherexpr) {
	if (otherexpr instanceof CPENot) {
	    return (cpe.equivalent(((CPENot)otherexpr).getCpe()));
	} else return false;
    }

}
