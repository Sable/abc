package abc.weaving.aspectinfo;

import soot.*;
import soot.jimple.*;

import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** Handler for <code>set</code> shadow pointcut. */
public class SetField extends AbstractShadowPointcutHandler {
    private FieldPattern pattern;

    public SetField(FieldPattern pattern) {
	this.pattern = pattern;
    }

    public FieldPattern getPattern() {
	return pattern;
    }

    static private ShadowType shadowType=new StmtShadowType();
    
    static {
	AbstractShadowPointcutHandler.registerShadowType(shadowType);
    }

    public ShadowType getShadowType() {
	return shadowType;
    }

    public Residue matchesAt(MethodPosition position) {
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
