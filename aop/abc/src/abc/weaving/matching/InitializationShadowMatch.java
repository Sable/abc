package abc.weaving.matching;

import soot.*;

import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.residues.Residue;

/** The results of matching at an initialization shadow
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public class InitializationShadowMatch extends ShadowMatch {
    private SootMethod container;

    private InitializationShadowMatch(SootMethod container) {
	this.container=container;
    }

    public static InitializationShadowMatch matchesAt(MethodPosition pos) {
	if(!(pos instanceof WholeMethodPosition)) return null;
	SootMethod container=((WholeMethodPosition) pos).container;
	if(!container.getName().equals(SootMethod.constructorName)) return null;
	return new InitializationShadowMatch(container);
    }


    public void addAdviceApplication(MethodAdviceList mal,
				     AdviceDecl ad,
				     Residue residue) {
	AdviceApplication.SJPInfo sjpInfo
	    = new AdviceApplication.SJPInfo
	    ("initialization","makeConstructorSig","",container);
	mal.addInitializationAdvice
	    (new InitializationAdviceApplication(ad,residue,sjpInfo));
    }
}
