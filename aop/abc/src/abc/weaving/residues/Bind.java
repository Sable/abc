package abc.weaving.residues;

import java.util.Vector;
import soot.Local;
import soot.Type;
import soot.SootMethod;
import soot.util.Chain;
import soot.jimple.Stmt;
import soot.jimple.Jimple;
import abc.weaving.weaver.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;

/** Bind a context value to a local or argument
 *  @author Ganesh Sittampalam
 *  @date 30-Apr-04
 */ 

public class Bind extends AbstractResidue {
    public ContextValue value;
    public WeavingVar variable;

    Bind(ContextValue value,WeavingVar variable) {
	this.value=value;
	this.variable=variable;
    }

    public static Residue construct(ContextValue value,Type type,WeavingVar variable) {
	return AndResidue.construct
	    (new CheckType(value,type),
	     new Bind(value,variable));
    }

    public String toString() {
	return "bind("+value+","+variable+")";
    }

    public Stmt codeGen(SootMethod method,LocalGeneratorEx localgen,
			Chain units,Stmt begin,Stmt fail,
			WeavingContext wc) {

	Type type=variable.getType();
	Local loc=localgen.generateLocal(type,"bind");
	Stmt castStmt=Jimple.v().newAssignStmt
	    (loc,Jimple.v().newCastExpr
	     (value.getSootValue(method,localgen),type));
	variable.set(wc,loc);
	units.insertAfter(castStmt,begin);
	return castStmt;
    }


}
