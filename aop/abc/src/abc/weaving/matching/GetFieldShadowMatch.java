package abc.weaving.matching;

import java.util.*;

import soot.*;
import soot.jimple.*;

import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.residues.Residue;
import abc.weaving.residues.ContextValue;
import abc.weaving.residues.JimpleValue;

/** The results of matching at a field get
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public class GetFieldShadowMatch extends StmtShadowMatch {
    
    private SootField field;
    
    private GetFieldShadowMatch(SootMethod container,Stmt stmt,SootField field) {
	super(container,stmt);
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

	return new GetFieldShadowMatch(pos.getContainer(),stmt,fr.getField());
    }
    
    public AdviceApplication.SJPInfo makeSJPInfo() {
	// FIXME : dummy details string
	return new AdviceApplication.SJPInfo
	    ("field-get","FieldSignature","makeFieldSig","0-r-Test-double-",stmt);
    }


    protected AdviceApplication doAddAdviceApplication
	(MethodAdviceList mal,AdviceDecl ad,Residue residue) {

	StmtAdviceApplication aa=new StmtAdviceApplication(ad,residue,stmt);
	mal.addStmtAdvice(aa);
	return aa;
    }

    public ContextValue getTargetContextValue() {
	FieldRef fr=(FieldRef) (((AssignStmt) stmt).getRightOp());
	if(!(fr instanceof InstanceFieldRef)) return null;
	InstanceFieldRef ifr=(InstanceFieldRef) fr;
	return new JimpleValue(ifr.getBase());
    }

    public ContextValue getReturningContextValue() {
	return new JimpleValue(((AssignStmt) stmt).getLeftOp());
    }

    public List/*<ContextValue>*/ getArgsContextValues() {
	return new ArrayList(0);
    }

}
