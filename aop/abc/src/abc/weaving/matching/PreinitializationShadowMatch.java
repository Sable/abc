package abc.weaving.matching;

import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.residues.Residue;

/** The results of matching at an preinitialization shadow
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public class PreinitializationShadowMatch extends ShadowMatch {
    private PreinitializationShadowMatch() {
    }

    public static PreinitializationShadowMatch matchesAt(MethodPosition pos) {
	if(!(pos instanceof WholeMethodPosition)) return null;
	return new PreinitializationShadowMatch();
    }


    public void addAdviceApplication(MethodAdviceList mal,
				     AdviceDecl ad,
				     Residue residue) {
	mal.preinitializationAdvice.add
	    (new PreinitializationAdviceApplication(ad,residue));
    }
}
