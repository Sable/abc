package abc.weaving.residues;

import soot.*;
import soot.util.Chain;
import soot.jimple.*;
import polyglot.util.InternalCompilerError;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;

/** A residue that tests if a local variable has a value
 *  @author Ganesh Sittampalam
 */ 

public class TestResidue extends Residue {
    
    Local loc;
    Value val;

    public TestResidue(Local l,Value v) {
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
	    throw new InternalCompilerError("TestResidue should never be used negated");

	Stmt test=Jimple.v().newIfStmt(Jimple.v().newNeExpr(loc,val),fail);
	units.insertAfter(test,begin);
	return test;
    }

    public String toString() {
	return "test("+loc+","+val+")";
    }

}
