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

    private CheckType(ContextValue value,Type type) {
	this.value=value;
	this.type=type;
    }

    // It's important that we throw away statically invalid matches
    // here rather than at code generation time, because if we wait until
    // then the code for a corresponding Bind will probably be generated
    // as well, and will be type incorrect; although it will be dead code,
    // the Soot code generator still won't be happy.
    // If it turns out that ContextValues need to be passed information
    // we don't have here, then we will need a mechanism for code generation
    // to prevent things that would be definitely dead code from being
    // generated at all.
    public static Residue construct(ContextValue value,Type type) {
	if(type.equals(Scene.v().getSootClass("java.lang.Object").getType())) 
	    return AlwaysMatch.v;

	if(type instanceof PrimType) {
	    if(type.equals(value.getSootValue().getType()))
		return AlwaysMatch.v;
	    else return NeverMatch.v;
	}

	return new CheckType(value,type);
    }

    public String toString() {
	return "checktype("+value+","+type+")";
    }

    public Stmt codeGen(SootMethod method,LocalGeneratorEx localgen,
			Chain units,Stmt begin,Stmt fail,
			WeavingContext wc) {

	Value v=value.getSootValue();
	Local io=localgen.generateLocal(BooleanType.v(),"checkType");
	Stmt instancetest
	    =Jimple.v().newAssignStmt(io,Jimple.v().newInstanceOfExpr(v,type));
	Stmt abort=Jimple.v().newIfStmt
	    (Jimple.v().newEqExpr(io,IntConstant.v(0)),fail);
	units.insertAfter(instancetest,begin);
	units.insertAfter(abort,instancetest);
	return abort;
    }

}
