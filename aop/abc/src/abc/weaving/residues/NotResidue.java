package abc.weaving.residues;

/** Disjunction of two residues
 *  @author Ganesh Sittampalam
 *  @date 28-Apr-04
 */ 
public class NotResidue extends AbstractResidue {
    private Residue op;
    
    /** Get the operand */
    public Residue getOp() {
	return op;
    }

        /** Private constructor to force use of smart constructor */
    private NotResidue(Residue op) {
	this.op=op;
    }

    /** Smart constructor; some short-circuiting may need to be removed
     *  to mimic ajc behaviour
     */
    public static Residue construct(Residue op) {
	if(op==null || op instanceof NeverMatch) return AlwaysMatch.v;
	if(op instanceof AlwaysMatch) return null;
	return new NotResidue(op);
    }
}
