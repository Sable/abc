package abc.weaving.matching;

import soot.jimple.*;
import soot.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.residues.Residue;

public class StmtAdviceApplication extends AdviceApplication {
    public Stmt stmt;
    
    public StmtAdviceApplication(AdviceDecl advice,
				 Residue residue,
				 Stmt stmt) {
	super(advice,residue);
	this.stmt=stmt;
    }
}
    
				      
