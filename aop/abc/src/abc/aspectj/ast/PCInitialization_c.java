package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class PCInitialization_c extends Pointcut_c implements PCInitialization
{
    protected ConstructorPattern pat;

    public PCInitialization_c(Position pos, ConstructorPattern pat)  {
	super(pos);
        this.pat = pat;
    }

    public Precedence precedence() {
	return Precedence.LITERAL;
    }

    protected PCInitialization_c reconstruct(ConstructorPattern pat) {
	if(pat != this.pat) {
	    PCInitialization_c n=(PCInitialization_c) copy();
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
	w.write("initialization(");
        print(pat, w, tr);
        w.write(")");
    }

    public abc.weaving.aspectinfo.Pointcut makeAIPointcut() {
	return new abc.weaving.aspectinfo.AndPointcut
	    (new abc.weaving.aspectinfo.WithinConstructor
	     (pat.makeAIConstructorPattern(),position()),
	     new abc.weaving.aspectinfo.Initialization(position()),
	     position());
    }

}
