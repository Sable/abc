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
public class PCSet_c extends Pointcut_c implements PCSet
{
    protected FieldPattern pat;

    public PCSet_c(Position pos, FieldPattern pat)  {
	super(pos);
        this.pat = pat;
    }

    public Precedence precedence() {
	return Precedence.LITERAL;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("set(");
        print(pat, w, tr);
        w.write(")");
    }

	public Set pcRefs() {
		return new HashSet();
	}
	
	public boolean isDynamic() {
		return false;
	}
	
    protected PCSet_c reconstruct(FieldPattern pat) {
	if (pat != this.pat) {
	    PCSet_c n = (PCSet_c) copy();
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
	return new abc.weaving.aspectinfo.SetField
	    (pat.makeAIFieldPattern(),position());
    }

}
