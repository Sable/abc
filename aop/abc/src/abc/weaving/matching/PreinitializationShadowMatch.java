package abc.weaving.matching;

import soot.*;

import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.residues.*;

/** The results of matching at an preinitialization shadow
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public class PreinitializationShadowMatch extends ShadowMatch {
    private PreinitializationShadowMatch() {
    }

    public static PreinitializationShadowMatch matchesAt(MethodPosition pos) {
	if(!(pos instanceof WholeMethodPosition)) return null;
	if(!((WholeMethodPosition) pos).container.getName().equals(SootMethod.constructorName)) return null;
	return new PreinitializationShadowMatch();
    }


    public void addAdviceApplication(MethodAdviceList mal,
				     AdviceDecl ad,
				     Residue residue) {
	AdviceApplication.SJPInfo sjpInfo
	    = new AdviceApplication.SJPInfo("preinitialization","makeConstructorSig","",-1,-1);
	mal.addPreinitializationAdvice
	    (new PreinitializationAdviceApplication(ad,residue,sjpInfo));
    }

    public ContextValue getThisContextValue(SootMethod method) {
        return null;
    }
}
