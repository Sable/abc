package abc.weaving.matching;

import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.residues.Residue;

/** for initialization pointcuts
 *  @author Ganesh Sittampalam
 *  @date 29-Apr-04
 */
public class InitializationAdviceApplication extends ConstructorAdviceApplication {
    public InitializationAdviceApplication(AbstractAdviceDecl advice,Residue residue) {
	super(advice,residue);
    }

    public void debugInfo(String prefix,StringBuffer sb) {
	sb.append(prefix+"initialization"+"\n");
	super.debugInfo(prefix,sb);
    }
}
