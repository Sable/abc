package abc.weaving.matching;

import soot.jimple.*;
import soot.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.residues.Residue;

public class HandlerAdviceApplication extends AdviceApplication {
    public Stmt stmt;

    public HandlerAdviceApplication(AdviceDecl advice,
				    Residue residue,
				    Stmt stmt) {
	super(advice,residue);
	this.stmt=stmt;
    }
}
    
				      
