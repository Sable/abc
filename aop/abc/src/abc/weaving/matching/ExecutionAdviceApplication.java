package abc.weaving.matching;

import soot.jimple.*;
import soot.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.residues.Residue;

public class ExecutionAdviceApplication extends AdviceApplication {
	
    public ExecutionAdviceApplication(AbstractAdviceDecl advice,Residue residue) {
	super(advice,residue);
    }

    public void debugInfo(String prefix,StringBuffer sb) {
	sb.append(prefix+"execution"+"\n");
	super.debugInfo(prefix,sb);
    }
}
    
				      
