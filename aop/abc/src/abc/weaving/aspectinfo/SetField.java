package abc.weaving.aspectinfo;

import soot.*;
import soot.jimple.*;

import polyglot.util.Position;

import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** Handler for <code>set</code> shadow pointcut. */
public class SetField extends ShadowPointcut {
    private FieldPattern pattern;

    public SetField(FieldPattern pattern,Position pos) {
	super(pos);
	this.pattern = pattern;
    }

    public FieldPattern getPattern() {
	return pattern;
    }

    static private ShadowType shadowType=new StmtShadowType();
    static public void registerShadowType() {
	ShadowPointcut.registerShadowType(shadowType);
    }

    public ShadowType getShadowType() {
	return shadowType;
    }

    protected Residue matchesAt(MethodPosition position) {
	if(!(position instanceof StmtMethodPosition)) return null;
	Stmt stmt=((StmtMethodPosition) position).getStmt();

	if(!(stmt instanceof AssignStmt)) return null;
	AssignStmt as = (AssignStmt) stmt;
	Value lhs = as.getLeftOp();
       	if(!(lhs instanceof FieldRef)) return null;
	FieldRef fr = (FieldRef) lhs;
	if(!getPattern().matchesField(fr.getField())) return null;
	return AlwaysMatch.v;
    }

    public String toString() {
	return "set("+pattern+")";
    }
}
