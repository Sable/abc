package abc.weaving.residues;

import soot.*;
import soot.jimple.*;
import soot.util.Chain;
import soot.jimple.Stmt;
import soot.jimple.IntConstant;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;

/** is a context value null?
 *  @author Ganesh Sittampalam
 */ 

public class IsNull extends Residue {
    private ContextValue value;

    public IsNull(ContextValue value) {
	this.value=value;
    }

    public String toString() {
	return "isnull("+value+")";
    }

    public Stmt codeGen(SootMethod method,LocalGeneratorEx localgen,
			Chain units,Stmt begin,Stmt fail,boolean sense,
			WeavingContext wc) {

	Value v=value.getSootValue();
	Expr test;
	if(sense) test=Jimple.v().newNeExpr(v,NullConstant.v());
	else test=Jimple.v().newEqExpr(v,NullConstant.v());
	Stmt abort=Jimple.v().newIfStmt(test,fail);
	units.insertAfter(abort,begin);
	return abort;
    }

}
