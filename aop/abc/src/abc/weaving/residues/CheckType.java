package abc.weaving.residues;

import soot.Type;
import soot.Value;
import soot.Local;
import soot.SootMethod;
import soot.BooleanType;
import soot.jimple.Jimple;
import soot.util.Chain;
import soot.jimple.Stmt;
import soot.jimple.IntConstant;
import abc.weaving.weaver.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;

/** Check the type of a context value
 *  @author Ganesh Sittampalam
 *  @date 30-Apr-04
 */ 

public class CheckType extends AbstractResidue {
    public ContextValue value;
    public Type type;

    public CheckType(ContextValue value,Type type) {
	this.value=value;
	this.type=type;
    }

    public String toString() {
	return "checktype("+value+","+type+")";
    }

    public Stmt codeGen(SootMethod method,LocalGeneratorEx localgen,
			Chain units,Stmt begin,Stmt fail,
			WeavingContext wc) {
	Value v=value.getSootValue(method,localgen);
	Local io=localgen.generateLocal(BooleanType.v(),"checkType");
	Stmt instancetest
	    =Jimple.v().newAssignStmt(io,Jimple.v().newInstanceOfExpr(v,type));
	Stmt abort=Jimple.v().newIfStmt
	    (Jimple.v().newEqExpr(io,IntConstant.v(0)),fail);
	units.insertAfter(instancetest,begin);
	if(false)	return instancetest;
	else {
	    units.insertAfter(abort,instancetest);
	    return abort;
	}
    }

}
