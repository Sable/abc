package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

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
	return new abc.weaving.aspectinfo.ShadowPointcut
	    (new abc.weaving.aspectinfo.StaticInitialization(pat.makeAIClassnamePattern()),
	     position());
    }
}
