package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;
import soot.jimple.*;

import abc.weaving.matching.MethodPosition;
import abc.weaving.residues.Residue;

/** A pointcut designator representing a condition on the 
 *  lexical context
 *  @author Ganesh Sittampalam
 *  @date 30-Apr-04
 */
public class LexicalPointcut extends AbstractPointcut {
    private LexicalPointcutHandler handler;

    public LexicalPointcut(LexicalPointcutHandler handler, Position pos) {
	super(pos);
	this.handler = handler;
    }

    public Residue matchesAt(ShadowType st,
			     SootClass cls,
			     SootMethod method,
			     MethodPosition position) {
	return handler.matchesAt(cls,method);
    }
}
