package abc.weaving.matching;

import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.residues.Residue;

/** The results of matching at an initialization shadow
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public class InitializationShadowMatch extends ShadowMatch {
    private InitializationShadowMatch() {
    }

    public static InitializationShadowMatch matchesAt(MethodPosition pos) {
	if(!(pos instanceof WholeMethodPosition)) return null;
	return new InitializationShadowMatch();
    }


    public void addAdviceApplication(MethodAdviceList mal,
				     AdviceDecl ad,
				     Residue residue) {
	AdviceApplication.SJPInfo sjpInfo
	    = new AdviceApplication.SJPInfo("initialization","makeConstructorSig",null,-1,-1);
	mal.addInitializationAdvice
	    (new InitializationAdviceApplication(ad,residue,sjpInfo));
    }
}
