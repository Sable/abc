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
public class NotResidue extends Residue {
    private Residue op;
    
    /** Get the operand */
    public Residue getOp() {
	return op;
    }

        /** Private constructor to force use of smart constructor */
    private NotResidue(Residue op) {
	this.op=op;
    }

    public String toString() {
	return "!("+op+")";
    }

    /** Smart constructor; some short-circuiting may need to be removed
     *  to mimic ajc behaviour
     */
    public static Residue construct(Residue op) {
	if(NeverMatch.neverMatches(op)) return AlwaysMatch.v;
	if(op instanceof AlwaysMatch) return null;
	return new NotResidue(op);
    }

    public Stmt codeGen(SootMethod method,LocalGeneratorEx localgen,
			Chain units,Stmt begin,Stmt fail,
			WeavingContext wc) {
	if(abc.main.Debug.v().residueCodeGen)
	    System.err.println("beginning not residue generation");
	Stmt nopStmt=Jimple.v().newNopStmt();
	Stmt notResidueEnd=op.codeGen(method,localgen,units,begin,nopStmt,wc);
	Stmt abort=Jimple.v().newGotoStmt(fail);
	units.insertAfter(abort,notResidueEnd);
	units.insertAfter(nopStmt,abort);
	if(abc.main.Debug.v().residueCodeGen)
	    System.err.println("done not residue generation");
	return nopStmt;
    }

	public void getAdviceFormalBindings(Bindings bindings) {
		getOp().getAdviceFormalBindings(bindings);
	}
	public Residue restructureToCreateBindingsMask(soot.Local bindingsMaskLocal, Bindings bindings) {
		op=op.restructureToCreateBindingsMask(bindingsMaskLocal, bindings);
		return this;
	}
}
