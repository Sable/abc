package abc.weaving.aspectinfo;

import soot.*;
import soot.jimple.*;

import polyglot.util.Position;

import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** Handler for <code>get</code> shadow pointcut. */
public class GetField extends ShadowPointcut {
    private FieldPattern pattern;

    public GetField(FieldPattern pattern,Position pos) {
	super(pos);
	this.pattern = pattern;
    }

    public FieldPattern getPattern() {
	return pattern;
    }

    static private ShadowType shadowType=new StmtShadowType();
    
    static {
	ShadowPointcut.registerShadowType(shadowType);
    }

    public ShadowType getShadowType() {
	return shadowType;
    }

    protected Residue matchesAt(MethodPosition position) {
	if(!(position instanceof StmtMethodPosition)) return null;
	Stmt stmt=((StmtMethodPosition) position).getStmt();

	if (!(stmt instanceof AssignStmt)) return null;
	AssignStmt as = (AssignStmt) stmt;
	Value rhs = as.getRightOp();
       	if(!(rhs instanceof FieldRef)) return null;
	FieldRef fr = (FieldRef) rhs;
	if(!getPattern().matchesField(fr.getField())) return null;
	return AlwaysMatch.v;
    }

    public String toString() {
	return "get("+pattern+")";
    }
}
