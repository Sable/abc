package abc.weaving.matching;

import soot.jimple.*;
import soot.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.residues.Residue;

public class NewStmtAdviceApplication extends AdviceApplication {
    public Stmt stmt;
    
    public NewStmtAdviceApplication(AbstractAdviceDecl advice,
				    Residue residue,
				    Stmt stmt) {
	super(advice,residue);
	this.stmt=stmt;
    }

    public void debugInfo(String prefix,StringBuffer sb) {
	sb.append(prefix+"new stmt: "+stmt+"\n");
	super.debugInfo(prefix,sb);
    }
}
    
				      
