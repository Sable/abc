package abc.weaving.matching;

import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.residues.Residue;

/** A base class for initialization and pre-initialization advice 
 *  @author Ganesh Sittampalam
 *  @date 29-Apr-04
 */

public abstract class ConstructorAdviceApplication extends AdviceApplication {
    public ConstructorAdviceApplication(AdviceDecl advice,Residue residue,SJPInfo sjpInfo) {
	super(advice,residue,sjpInfo);
    }
}
