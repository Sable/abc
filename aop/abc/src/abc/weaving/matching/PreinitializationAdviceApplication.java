package abc.weaving.matching;

import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.residues.Residue;

public class PreinitializationAdviceApplication 
    extends ConstructorAdviceApplication {
    public PreinitializationAdviceApplication(AdviceDecl advice,Residue residue,SJPInfo sjpInfo) {
	super(advice,residue,sjpInfo);
    }
}
