package abc.weaving.residues;

/** A "dynamic" residue that can never match. 
 *  Intended for convenience during generation and residue analysis process.
 *  @author Ganesh Sittampalam
 *  @date 28-Apr-04
 */ 

public class AlwaysMatch extends AbstractResidue {
    // is this worthwhile? (save on heap turnover)
    public final static AlwaysMatch v=new AlwaysMatch();

    public String toString() {
	return "always";
    }
}
