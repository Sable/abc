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
	
	public boolean isDynamic() {
		return false;
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
	abc.weaving.aspectinfo.Pointcut pc=abc.weaving.aspectinfo.AndPointcut.construct
	    (new abc.weaving.aspectinfo.WithinConstructor
	     (pat.makeAIConstructorPattern(),position()),
	     new abc.weaving.aspectinfo.ClassInitialization(position()),
	     position());

	if(pat.canMatchEmptyArgumentList()) 
	    pc=abc.weaving.aspectinfo.OrPointcut.construct
		(pc,
		 new abc.weaving.aspectinfo.InterfaceInitialization
		 (pat.getName().base().makeAIClassnamePattern(),position()),
		 position());
	return pc;
    }

}
