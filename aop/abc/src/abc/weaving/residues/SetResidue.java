package abc.weaving.residues;

import soot.*;
import soot.util.Chain;
import soot.jimple.*;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;

/** A residue that sets a local variable to a value
 *  @author Ganesh Sittampalam
 */ 

public class SetResidue extends Residue {
    
    Local loc;
    Value val;

    public SetResidue(Local l,Value v) {
	loc=l;
	val=v;
    }

    public Stmt codeGen(SootMethod method,LocalGeneratorEx localgen,
			Chain units,Stmt begin,Stmt fail,
			WeavingContext wc) {

	Stmt assign=Jimple.v().newAssignStmt(loc,val);
	units.insertAfter(assign,begin);
	return assign;
    }

    public String toString() {
	return "set("+loc+","+val+")";
    }

}
