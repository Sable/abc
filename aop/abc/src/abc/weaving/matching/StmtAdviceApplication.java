package abc.weaving.matching;

import soot.jimple.*;
import soot.util.*;
import abc.weaving.aspectinfo.*;

public class StmtAdviceApplication extends AdviceApplication {
    public Stmt stmt;
    
    public StmtAdviceApplication(AdviceDecl advice,
				 ConditionPointcutHandler cph,
				 Stmt stmt) {
	super(advice,cph);
	this.stmt=stmt;
    }
}
    
				      
