package abc.weaving.matching;

import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.residues.Residue;

/** A base class for initialization and pre-initialization advice 
 *  @author Ganesh Sittampalam
 */

public abstract class ConstructorAdviceApplication extends AdviceApplication {
    public ConstructorAdviceApplication(AbstractAdviceDecl advice,Residue residue) {
	super(advice,residue);
    }
}
