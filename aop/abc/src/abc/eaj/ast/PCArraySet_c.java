package abc.eaj.ast;

import java.util.Set;
import java.util.HashSet;

import polyglot.ast.Precedence;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.PrettyPrinter;
import abc.weaving.aspectinfo.Pointcut;
import abc.aspectj.ast.Pointcut_c;

/**
 * @author Pavel Avgustinov
 */

public class PCArraySet_c extends Pointcut_c implements PCArraySet {
	public PCArraySet_c(Position pos) {
		super(pos);
	}

	public boolean isDynamic() {
		return false;
	}

    public Set pcRefs() {
        return new HashSet();
    }
	
	public Pointcut makeAIPointcut() {
		return new abc.eaj.weaving.aspectinfo.ArraySet(position());
	}

	public Precedence precedence() {
		return Precedence.LITERAL;
	}

	public void dump(CodeWriter w) {
		w.write("arrayset()");
	}

	public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
		w.write("arrayset()");
	}

	protected PCArraySet_c reconstruct() {
		return this;
	}
}
