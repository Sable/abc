package abc.weaving.residues;

import soot.*;
import soot.util.Chain;
import soot.jimple.*;
import polyglot.util.InternalCompilerError;
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
			Chain units,Stmt begin,Stmt fail,boolean sense,
			WeavingContext wc) {

	// We don't expect the frontend/matcher to produce a residue that does this. 
	// There's no reason we couldn't just do the standard "automatic fail" thing 
	// if there was ever a need, though.
	if(!sense) 
	    throw new InternalCompilerError("SetResidue should never be used negated");


	Stmt assign=Jimple.v().newAssignStmt(loc,val);
	units.insertAfter(assign,begin);
	return assign;
    }

    public String toString() {
	return "set("+loc+","+val+")";
    }

}
