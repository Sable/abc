package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class PCPreinitialization_c extends Pointcut_c 
                                   implements PCPreinitialization
{
    protected ConstructorPattern pat;

    public PCPreinitialization_c(Position pos, ConstructorPattern pat)  {
	super(pos);
        this.pat = pat;
    }

    public Precedence precedence() {
	return Precedence.LITERAL;
    }

    protected PCPreinitialization_c reconstruct(ConstructorPattern pat) {
	if(pat != this.pat) {
	    PCPreinitialization_c n=(PCPreinitialization_c) copy();
	    n.pat=pat;
	    return n;
	}
	return this;
    }
    
	public Set pcRefs() {
		return new HashSet();
	}
	
    public Node visitChildren(NodeVisitor v) {
	ConstructorPattern pat
	    = (ConstructorPattern) visitChild(this.pat,v);
	return reconstruct(pat);
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("preinitialization(");
        print(pat, w, tr);
        w.write(")");
    }

    public abc.weaving.aspectinfo.Pointcut makeAIPointcut() {
	return abc.weaving.aspectinfo.AndPointcut.construct
	    (new abc.weaving.aspectinfo.WithinConstructor
	     (pat.makeAIConstructorPattern(),position()),
	     new abc.weaving.aspectinfo.Preinitialization(position()),
	     position());
    }

}
