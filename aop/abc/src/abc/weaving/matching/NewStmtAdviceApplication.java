package abc.weaving.matching;

import soot.jimple.*;
import soot.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.residues.Residue;

public class NewStmtAdviceApplication extends AdviceApplication {
    public Stmt stmt;
    
    public NewStmtAdviceApplication(AdviceDecl advice,
				    Residue residue,
				    SJPInfo sjpInfo,
				    Stmt stmt) {
	super(advice,residue,sjpInfo);
	this.stmt=stmt;
    }

    public void debugInfo(String prefix,StringBuffer sb) {
	sb.append(prefix+"new stmt: "+stmt+"\n");
	super.debugInfo(prefix,sb);
    }
}
    
				      
