package abc.weaving.residues;

/** A "dynamic" residue that can never match. 
 *  Intended for convenience during generation and residue analysis process.
 *  Can also use null to represent this; need to decide whether keeping
 *  it is worthwhile.
 *  @author Ganesh Sittampalam
 *  @date 28-Apr-04
 */ 

public class NeverMatch extends AbstractResidue {
    // is this worthwhile? (save on heap turnover)
    public final static NeverMatch v=new NeverMatch();

    public static boolean neverMatches(Residue r) {
	return r==null || r instanceof NeverMatch;
    }

    public String toString() {
	return "never";
    }
}
