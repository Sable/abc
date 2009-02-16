package abc.impact.impact;

import soot.SootMethod;
import soot.jimple.Stmt;

public class ControlFlowEvidence {
	public static final int EXCEPTION = 1;
	public static final int RETURN = 2;
	public static final int FORMAL = 3;
	public static final int EXACTPROCEED = 4;
	
	public SootMethod sootMethod;
	public Stmt stmt;
	public int type;
	
	public ControlFlowEvidence(SootMethod sootMethod, Stmt stmt, int type) {
		this.sootMethod = sootMethod;
		this.stmt = stmt;
		this.type = type;
	}
}