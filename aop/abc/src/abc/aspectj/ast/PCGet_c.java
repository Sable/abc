package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class PCGet_c extends Pointcut_c implements PCGet
{
    protected FieldPattern pat;

    public PCGet_c(Position pos, FieldPattern pat)  {
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
	
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("get(");
        print(pat, w, tr);
        w.write(")");
    }

    protected PCGet_c reconstruct(FieldPattern pat) {
	if (pat != this.pat) {
	    PCGet_c n = (PCGet_c) copy();
	    n.pat = pat;
	    return n;
	}
	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	FieldPattern pat=(FieldPattern) visitChild(this.pat,v);
	return reconstruct(pat);
    }

    public abc.weaving.aspectinfo.Pointcut makeAIPointcut() {
	return new abc.weaving.aspectinfo.GetField
	    (pat.makeAIFieldPattern(),position());
    }

}
