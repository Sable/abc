package abc.weaving.matching;

import java.util.*;

import soot.*;
import soot.jimple.*;

import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.residues.Residue;
import abc.weaving.residues.ContextValue;
import abc.weaving.residues.JimpleValue;

/** The results of matching at a field set
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public class SetFieldShadowMatch extends StmtShadowMatch {
    
    private SootField field;

    private SetFieldShadowMatch(SootMethod container,Stmt stmt,SootField field) {
	super(container,stmt);
	this.field=field;
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
	// FIXME : dummy details string
	return new AdviceApplication.SJPInfo
	    ("field-set","FieldSignature","makeFieldSig",
	     "8-t-Test-Test-",stmt);
    }

    protected AdviceApplication doAddAdviceApplication
	(MethodAdviceList mal,AdviceDecl ad,Residue residue) {

	StmtAdviceApplication aa=new StmtAdviceApplication(ad,residue,stmt);
	mal.addStmtAdvice(aa);
	return aa;
    }

    public ContextValue getTargetContextValue() {
	FieldRef fr=(FieldRef) (((AssignStmt) stmt).getLeftOp());
	if(!(fr instanceof InstanceFieldRef)) return null;
	InstanceFieldRef ifr=(InstanceFieldRef) fr;
	return new JimpleValue(ifr.getBase());
    }

    public List/*<ContextValue>*/ getArgsContextValues() {
	ArrayList ret=new ArrayList(1);
	ret.add(new JimpleValue(((AssignStmt) stmt).getRightOp()));
	return ret;
    }

}
