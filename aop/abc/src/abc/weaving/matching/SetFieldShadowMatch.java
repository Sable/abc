package abc.weaving.matching;

import soot.*;
import soot.jimple.*;

import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.residues.Residue;

/** The results of matching at a field set
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public class SetFieldShadowMatch extends ShadowMatch {
    
    private Stmt stmt;
    private SootField field;

    private SetFieldShadowMatch(SootMethod container,Stmt stmt,SootField field) {
	super(container);
	this.stmt=stmt;
	this.field=field;
    }

    public ShadowMatch getEnclosing() {
	if(stmt.hasTag(abc.soot.util.InPreinitializationTag.name)) return this;
	return new ExecutionShadowMatch(container);
    }

    public SootField getField() {
	return field;
    }

    public static SetFieldShadowMatch matchesAt(MethodPosition pos) {
	if(!(pos instanceof StmtMethodPosition)) return null;

	Stmt stmt=((StmtMethodPosition) pos).getStmt();
	if(!(stmt instanceof AssignStmt)) return null;
	AssignStmt as = (AssignStmt) stmt;
	Value lhs = as.getLeftOp();
       	if(!(lhs instanceof FieldRef)) return null;
	FieldRef fr = (FieldRef) lhs;

	return new SetFieldShadowMatch(pos.getContainer(),stmt,fr.getField());
    }

    public AdviceApplication.SJPInfo makeSJPInfo() {

	return new AdviceApplication.SJPInfo
	    ("field-set","FieldSignature","makeFieldSig","",stmt);
    }

    protected AdviceApplication doAddAdviceApplication
	(MethodAdviceList mal,AdviceDecl ad,Residue residue) {

	StmtAdviceApplication aa=new StmtAdviceApplication(ad,residue,stmt);
	mal.addStmtAdvice(aa);
	return aa;
    }

}
