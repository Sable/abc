package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;

import java.util.*;

public class PCExecution_c extends Pointcut_c implements PCExecution
{
    protected MethodConstructorPattern pat;

    public PCExecution_c(Position pos, MethodConstructorPattern pat)  {
	super(pos);
        this.pat = pat;
    }

    public Precedence precedence() {
	return Precedence.LITERAL;
    }

    protected PCExecution_c reconstruct(MethodConstructorPattern pat) {
	if(pat != this.pat) {
	    PCExecution_c n=(PCExecution_c) copy();
	    n.pat=pat;
	    return n;
	}
	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	MethodConstructorPattern pat
	    = (MethodConstructorPattern) visitChild(this.pat,v);
	return reconstruct(pat);
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("execution (");
        print(pat, w, tr);
        w.write(")");
    }

    public abc.weaving.aspectinfo.Pointcut makeAIPointcut() {
	abc.weaving.aspectinfo.Pointcut withincode;
	if (pat instanceof MethodPattern) {
	    withincode=new abc.weaving.aspectinfo.WithinMethod
		(((MethodPattern)pat).makeAIMethodPattern(),
		 position());
	} else if (pat instanceof ConstructorPattern) {
	    withincode=new abc.weaving.aspectinfo.WithinConstructor
		(((ConstructorPattern)pat).makeAIConstructorPattern(),
		 position());
	} else {
	    throw new RuntimeException
		("Unexpected MethodConstructorPattern type in execution pointcut: "+pat);
	}
	return (new abc.weaving.aspectinfo.AndPointcut
		(withincode,
		 new abc.weaving.aspectinfo.Execution(position()),
		 position()));
    }
}
