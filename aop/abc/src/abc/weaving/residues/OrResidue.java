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
			Chain units,Stmt begin,Stmt fail,
			WeavingContext wc) {

	Stmt nopStmt1=Jimple.v().newNopStmt();
	Stmt leftResidueEnd=left.codeGen(method,localgen,units,begin,nopStmt1,wc);
	Stmt nopStmt2=Jimple.v().newNopStmt();
	Stmt succeedEarly=Jimple.v().newGotoStmt(nopStmt2);
	units.insertAfter(succeedEarly,leftResidueEnd);
	units.insertAfter(nopStmt1,succeedEarly);
	Stmt rightResidueEnd=right.codeGen(method,localgen,units,nopStmt1,fail,wc);
	units.insertAfter(nopStmt2,rightResidueEnd);
	return nopStmt2;
    }

}
