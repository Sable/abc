package abc.weaving.matching;

import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.residues.Residue;

/** for initialization pointcuts
 *  @author Ganesh Sittampalam
 *  @date 29-Apr-04
 */
public class InitializationAdviceApplication extends ConstructorAdviceApplication {
    public InitializationAdviceApplication(AdviceDecl advice,Residue residue,SJPInfo sjpInfo) {
	super(advice,residue,sjpInfo);
    }

    public void debugInfo(String prefix,StringBuffer sb) {
	sb.append(prefix+"initialization"+"\n");
	super.debugInfo(prefix,sb);
    }
}
