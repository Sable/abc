package abc.weaving.matching;

import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.residues.Residue;

/** For initialization pointcuts
 *  @author Ganesh Sittampalam
 */
public class ClassInitializationAdviceApplication extends ConstructorAdviceApplication {
    public ClassInitializationAdviceApplication(AbstractAdviceDecl advice,Residue residue) {
	super(advice,residue);
    }

    public void debugInfo(String prefix,StringBuffer sb) {
	sb.append(prefix+"classinitialization"+"\n");
	super.debugInfo(prefix,sb);
    }
}
