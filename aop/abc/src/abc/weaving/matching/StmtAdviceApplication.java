package abc.weaving.matching;

import soot.jimple.*;
import soot.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.residues.Residue;

public class StmtAdviceApplication extends AdviceApplication {
    public Stmt stmt;
    
    public StmtAdviceApplication(AdviceDecl advice,
				 Residue residue,
				 SJPInfo sjpInfo,
				 Stmt stmt) {
	super(advice,residue,sjpInfo);
	this.stmt=stmt;
    }

    public void debugInfo(String prefix,StringBuffer sb) {
	sb.append(prefix+"stmt: "+stmt+"\n");
	super.debugInfo(prefix,sb);
    }

    public String toString() {
	return "stmt : "+stmt;
    }
}
    
				      
