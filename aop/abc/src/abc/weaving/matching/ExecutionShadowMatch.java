package abc.weaving.matching;

import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.residues.Residue;

/** The results of matching at an execution shadow
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public class ExecutionShadowMatch extends ShadowMatch {
    private ExecutionShadowMatch() {
    }

    public static ExecutionShadowMatch matchesAt(MethodPosition pos) {
	if(!(pos instanceof WholeMethodPosition)) return null;
	return new ExecutionShadowMatch();
    }

    public void addAdviceApplication(MethodAdviceList mal,
				     AdviceDecl ad,
				     Residue residue) {
	mal.addBodyAdvice(new ExecutionAdviceApplication(ad,residue));
    }
}
