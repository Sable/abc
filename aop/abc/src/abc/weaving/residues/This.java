package abc.weaving.residues;

import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.jimple.Stmt;
import soot.jimple.IdentityStmt;
import soot.jimple.ThisRef;
import soot.jimple.Jimple;
import soot.util.Chain;
import abc.weaving.weaver.LocalGeneratorEx;
import abc.weaving.weaver.PointcutCodeGen;

/** "this" context value
 *  @author Ganesh Sittampalam
 *  @date 30-Apr-04
 */ 

public class This extends AbstractContextValue {
    public String toString() {
	return "this";
    }

    public Value getSootValue(SootMethod method,LocalGeneratorEx localgen) {
	Local l;
	if(PointcutCodeGen.thisLocalMap.containsKey(method)) {
	    l=(Local) PointcutCodeGen.thisLocalMap.get(method);
	} else {
	    l=localgen.generateLocal(method.getDeclaringClass().getType(),"thisCopy");

	    Chain units=method.getActiveBody().getUnits();
	    for(Stmt stmt=(Stmt) units.getFirst();
		;
		stmt=(Stmt) units.getSuccOf(stmt)) {

		if(stmt==null) throw new RuntimeException
				   ("internal error: didn't find identitystmt binding this "
				    +"in method "+method+" inside which this() advice applied");

		if(stmt instanceof IdentityStmt) {
		    IdentityStmt istmt=(IdentityStmt) stmt;
		    if(istmt.getRightOp() instanceof ThisRef) {
			Value tr=istmt.getLeftOp();
			units.insertAfter(Jimple.v().newAssignStmt(l,tr),stmt);
			break;
		    }
		}
	    }

	    PointcutCodeGen.thisLocalMap.put(method,l);
	}
	return l;
    }
}
