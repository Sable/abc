package abc.weaving.residues;

import soot.SootMethod;
import soot.util.Chain;
import soot.jimple.*;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;

/** Disjunction of two residues
 *  @author Ganesh Sittampalam
 *  @date 28-Apr-04
 */ 
public class OrResidue extends Residue {
    private Residue left;
    private Residue right;

    /** Get the left operand */
    public Residue getLeftOp() {
	return left;
    }

    /** Get the right operand */
    public Residue getRightOp() {
	return right;
    }

    public String toString() {
	return "("+left+") || ("+right+")";
    }

    /** Private constructor to force use of smart constructor */
    private OrResidue(Residue left,Residue right) {
	this.left=left;
	this.right=right;
    }

    /** Smart constructor; some short-circuiting may need to be removed
     *  to mimic ajc behaviour
     */
    public static Residue construct(Residue left,Residue right) {
	if(NeverMatch.neverMatches(left) || right instanceof AlwaysMatch) 
	    return right;
	if(left instanceof AlwaysMatch || NeverMatch.neverMatches(right))
	    return left;
	return new OrResidue(left,right);
    }

    public Stmt codeGen(SootMethod method,LocalGeneratorEx localgen,
			Chain units,Stmt begin,Stmt fail,boolean sense,
			WeavingContext wc) {
	if(sense) {
	    // want to fall through if either left or right succeeds, otherwise jump to fail
	    Stmt nopStmt=Jimple.v().newNopStmt();
	    // if left succeeds, goto nop stmt, otherwise fall through
	    Stmt middle=left.codeGen(method,localgen,units,begin,nopStmt,false,wc);
	    // if right succeeds fall through, otherwise then jump to fail
	    Stmt end=right.codeGen(method,localgen,units,middle,fail,true,wc);
	    // make fall through statement be the nop to catch the left residue succeeding
	    units.insertAfter(nopStmt,end);
	    return nopStmt;
	} else {
	    // want to jump to fail if either left or right succeeds, otherwise fall through
	    Stmt middle=left.codeGen(method,localgen,units,begin,fail,false,wc);
	    return right.codeGen(method,localgen,units,middle,fail,false,wc);
	}
    }

	public void getAdviceFormalBindings(Bindings bindings) {
		getLeftOp().getAdviceFormalBindings(bindings);
		getRightOp().getAdviceFormalBindings(bindings);
	}
	public Residue restructureToCreateBindingsMask(soot.Local bindingsMaskLocal, Bindings bindings) {
		left=left.restructureToCreateBindingsMask(bindingsMaskLocal, bindings);
		right=right.restructureToCreateBindingsMask(bindingsMaskLocal, bindings);
		return this;
	}
}
