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
			Chain units,Stmt begin,Stmt fail,
			WeavingContext wc) {

	Value v=value.getSootValue();
	Stmt abort=Jimple.v().newIfStmt
	    (Jimple.v().newNeExpr(v,NullConstant.v()),fail);
	units.insertAfter(abort,begin);
	return abort;
    }

}