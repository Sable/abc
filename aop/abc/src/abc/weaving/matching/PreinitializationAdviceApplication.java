package abc.weaving.matching;

import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.residues.Residue;

public class PreinitializationAdviceApplication 
    extends ConstructorAdviceApplication {
    public PreinitializationAdviceApplication(AdviceDecl advice,Residue residue,SJPInfo sjpInfo) {
	super(advice,residue,sjpInfo);
    }

    public void debugInfo(String prefix,StringBuffer sb) {
	sb.append(prefix+"preinitialization"+"\n");
	super.debugInfo(prefix,sb);
    }
}
