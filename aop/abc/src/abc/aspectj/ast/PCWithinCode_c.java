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
public class PCWithinCode_c extends Pointcut_c implements PCWithinCode
{
    protected MethodConstructorPattern pat;

    public PCWithinCode_c(Position pos, MethodConstructorPattern pat)  {
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
	
    protected PCWithinCode_c reconstruct(MethodConstructorPattern pat) {
	if (pat != this.pat) {
	    PCWithinCode_c n = (PCWithinCode_c) copy();
	    n.pat = pat;
	    return n;
	}
	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	MethodConstructorPattern pat=
	    (MethodConstructorPattern) visitChild(this.pat,v);
	return reconstruct(pat);
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("withincode(");
        print(pat, w, tr);
        w.write(")");
    }

    public abc.weaving.aspectinfo.Pointcut makeAIPointcut() {
	if (pat instanceof MethodPattern) {
	    return new abc.weaving.aspectinfo.WithinMethod
		(((MethodPattern)pat).makeAIMethodPattern(),
		 position());
	} else if (pat instanceof ConstructorPattern) {
	    return new abc.weaving.aspectinfo.WithinConstructor
		(((ConstructorPattern)pat).makeAIConstructorPattern(),
		 position());
	} else {
	    throw new RuntimeException
		("Unexpected MethodConstructorPattern type in withincode pointcut: "+pat);
	}
    }
}
