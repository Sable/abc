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
public class PCStaticInitialization_c extends Pointcut_c 
    implements PCStaticInitialization
{
    protected ClassnamePatternExpr pat;

    public PCStaticInitialization_c(Position pos, 
                                    ClassnamePatternExpr pat)  {
	super(pos);
        this.pat = pat;
    }

    public Precedence precedence() {
	return Precedence.LITERAL;
    }

    protected PCStaticInitialization_c reconstruct(ClassnamePatternExpr pat) {
	if (pat != this.pat) {
	    PCStaticInitialization_c n = (PCStaticInitialization_c) copy();
	    n.pat = pat;
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
	ClassnamePatternExpr pat=
	    (ClassnamePatternExpr) visitChild(this.pat,v);
	return reconstruct(pat);
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("staticinitialization(");
        print(pat, w, tr);
        w.write(")");
    }

    public abc.weaving.aspectinfo.Pointcut makeAIPointcut() {
	return abc.weaving.aspectinfo.AndPointcut.construct
	    (new abc.weaving.aspectinfo.DirectlyWithin(pat.makeAIClassnamePattern(),position()),
	     abc.weaving.aspectinfo.AndPointcut.construct
	     (new abc.weaving.aspectinfo.WithinStaticInitializer(position()),
	      new abc.weaving.aspectinfo.Execution(position()),
	      position()),
	     position());
						    
    }
}
