package abc.weaving.matching;

import soot.*;
import soot.jimple.*;

import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.residues.Residue;

/** The results of matching at a field get
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public class GetFieldShadowMatch extends ShadowMatch {
    
    private Stmt stmt;
    private SootField field;
    
    private GetFieldShadowMatch(Stmt stmt,SootField field) {
	this.stmt=stmt;
	this.field=field;
    }

    public SootField getField() {
	return field;
    }

    public static GetFieldShadowMatch matchesAt(MethodPosition pos) {
	if(!(pos instanceof StmtMethodPosition)) return null;

	Stmt stmt=((StmtMethodPosition) pos).getStmt();

	if (!(stmt instanceof AssignStmt)) return null;
	AssignStmt as = (AssignStmt) stmt;
	Value rhs = as.getRightOp();
       	if(!(rhs instanceof FieldRef)) return null;
	FieldRef fr = (FieldRef) rhs;

	return new GetFieldShadowMatch(stmt,fr.getField());
    }

    public void addAdviceApplication(MethodAdviceList mal,
				     AdviceDecl ad,
				     Residue residue) {
        mal.stmtAdvice.add(new StmtAdviceApplication(ad,residue,stmt));
    }
}
