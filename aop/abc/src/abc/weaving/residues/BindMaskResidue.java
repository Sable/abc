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
 * Generates code to set bits in a mask that
 * express this particular binding.
 */
public class BindMaskResidue extends Residue {

	private Residue op;
	private Local bindMaskLocal;
	private int mask;
	BindMaskResidue(Bind bind, Local bindMaskLocal, int mask) {
		this.op=bind;
		this.bindMaskLocal=bindMaskLocal;
		this.mask=mask;
	}
	/**
	 * Generates code to set the bits in the mask, then
	 * generates the Bind code. 
	 * 
	 */
	public Stmt codeGen(SootMethod method, LocalGeneratorEx localgen, Chain units, Stmt begin, Stmt fail, 
			    boolean sense, WeavingContext wc) {
		AssignStmt as=Jimple.v().newAssignStmt(
			bindMaskLocal, Jimple.v().newOrExpr(bindMaskLocal, IntConstant.v(mask)));
		units.insertAfter(as, begin);
		return op.codeGen(method, localgen, units, as, fail, sense, wc);
	}

	/* 
	 * 
	 */
	public String toString() {
		return "bindmask(" + op  + ")";
	}
	
	public void getAdviceFormalBindings(Bindings bindings) {
		op.getAdviceFormalBindings(bindings);
	}
	public Residue restructureToCreateBindingsMask(soot.Local bindingsMaskLocal, Bindings bindings) {
		op=op.restructureToCreateBindingsMask(bindingsMaskLocal, bindings);
		return this;
	}

}
