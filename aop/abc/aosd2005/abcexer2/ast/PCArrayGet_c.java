/*
 * Created on 09-Feb-2005
 */
package abcexer2.ast;

import java.util.HashSet;
import java.util.Set;

import polyglot.ast.Precedence;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.PrettyPrinter;

import abc.aspectj.ast.Pointcut_c;
import abc.weaving.aspectinfo.Pointcut;

/**
 * @author sascha
 *
 */
public class PCArrayGet_c extends Pointcut_c implements PCArrayGet {

	public PCArrayGet_c(Position pos) {
		super(pos);
	}
	public boolean isDynamic() {
		return false;
	}

	public Pointcut makeAIPointcut() {
		 return new abcexer2.weaving.aspectinfo.ArrayGet(position());
	}

	public Precedence precedence()
    {
        return Precedence.LITERAL; /// is this correct?
    }
	public void prettyPrint(CodeWriter w, PrettyPrinter pp)
    {
        w.write("arrayget()");
    }

	public Set pcRefs() {
		return new HashSet();
	}

}
