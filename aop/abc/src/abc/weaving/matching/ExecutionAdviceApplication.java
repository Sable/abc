package abc.weaving.matching;

import soot.jimple.*;
import soot.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.residues.Residue;

public class ExecutionAdviceApplication extends AdviceApplication {
	
    public ExecutionAdviceApplication(AdviceDecl advice,Residue residue) {
	super(advice,residue);
    }
}
    
				      
