package abc.weaving.residues;

import soot.*;
import soot.jimple.Jimple;
import soot.util.Chain;
import soot.jimple.Stmt;
import soot.jimple.IntConstant;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;

/** Check the type of a context value
 *  @author Ganesh Sittampalam
 *  @date 30-Apr-04
 */ 

public class CheckType extends Residue {
    public ContextValue value;
    public Type type;

    CheckType(ContextValue value,Type type) {
	this.value=value;
	this.type=type;
    }

    public static Residue construct(ContextValue value,Type type) {
	if(type.equals(Scene.v().getSootClass("java.lang.Object").getType())) return AlwaysMatch.v;
	else return new CheckType(value,type);
    }

    public String toString() {
	return "checktype("+value+","+type+")";
    }

    public Stmt codeGen(SootMethod method,LocalGeneratorEx localgen,
			Chain units,Stmt begin,Stmt fail,
			WeavingContext wc) {
	Value v=value.getSootValue(method,localgen);
	Local io=localgen.generateLocal(BooleanType.v(),"checkType");
	if(type instanceof PrimType) {
	    if(type.equals(v.getType())) return begin;
	    else {
		Stmt abort=Jimple.v().newGotoStmt(fail);
		units.insertAfter(abort,begin);
		return abort;
	    }
	}
	Stmt instancetest
	    =Jimple.v().newAssignStmt(io,Jimple.v().newInstanceOfExpr(v,type));
	Stmt abort=Jimple.v().newIfStmt
	    (Jimple.v().newEqExpr(io,IntConstant.v(0)),fail);
	units.insertAfter(instancetest,begin);
	units.insertAfter(abort,instancetest);
	return abort;
    }

}
