/*
 * Created on Jul 6, 2004
 *
 */
package abc.weaving.residues;

import soot.Local;
import soot.SootMethod;
import soot.jimple.AssignStmt;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;

/**
 * @author Sascha Kuzins
 * 
 * Needed for ambiguous bindings.
 * Always contains a Bind residue.
 * Generates code to set bits in a mask that
 * express this particular binding.
 */
public class BindMaskResidue extends Residue {

	private Bind bind;
	private Local bindMaskLocal;
	private int mask;
	BindMaskResidue(Bind bind, Local bindMaskLocal, int mask) {
		this.bind=bind;
		this.bindMaskLocal=bindMaskLocal;
		this.mask=mask;
	}
	/**
	 * Generates code to set the bits in the mask, then
	 * generates the Bind code. 
	 * 
	 */
	public Stmt codeGen(SootMethod method, LocalGeneratorEx localgen, Chain units, Stmt begin, Stmt fail, WeavingContext wc) {
		AssignStmt as=Jimple.v().newAssignStmt(
			bindMaskLocal, Jimple.v().newOrExpr(bindMaskLocal, IntConstant.v(mask)));
		units.insertAfter(as, begin);
		return bind.codeGen(method, localgen, units, as, fail, wc);
	}

	/* 
	 * 
	 */
	public String toString() {
		return "bindmask(" + bind  + ")";
	}

}
