package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

/**
 * 
 * @author Oege de Moor
 *
 */
public class PCHandler_c extends Pointcut_c implements PCHandler
{
    protected ClassnamePatternExpr pat;

    public PCHandler_c(Position pos, ClassnamePatternExpr pat)  {
	super(pos);
        this.pat = pat;
    }

    public Precedence precedence() {
	return Precedence.LITERAL;
    }
    
	public Set pcRefs() {
		return new HashSet();
	}
	
	public boolean isDynamic() {
		return false;
	}

    protected PCHandler_c reconstruct(ClassnamePatternExpr pat) {
	if (pat != this.pat) {
	    PCHandler_c n = (PCHandler_c) copy();
	    n.pat = pat;
	    return n;
	}
	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	ClassnamePatternExpr pat=
	    (ClassnamePatternExpr) visitChild(this.pat,v);
	return reconstruct(pat);
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("handler(");
        print(pat, w, tr);
        w.write(")");
    }

    public abc.weaving.aspectinfo.Pointcut makeAIPointcut() {
	return new abc.weaving.aspectinfo.Handler
	    (pat.makeAIClassnamePattern(),position());
    }
}
