package abc.weaving.residues;

/** A box holding a residue.
 *  @author Ondrej Lhotak
 *  @date 3-Aug-04
 */ 

public class ResidueBox {
    private Residue residue;

     /** Sets the residue contained in this box as given. */
    public void setResidue(Residue residue) {
        this.residue = residue;
    }

    /** Returns the residue contained in this box. */
    public Residue getResidue() {
        return residue;
    }

    public String toString() {
        return residue.toString();
    }

}
