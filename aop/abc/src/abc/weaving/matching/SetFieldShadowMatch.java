package abc.weaving.matching;

import java.util.ArrayList;
import java.util.List;

import soot.Body;
import soot.Local;
import soot.SootField;
import soot.SootMethod;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.residues.ContextValue;
import abc.weaving.residues.JimpleValue;
import abc.weaving.residues.Residue;

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

	makeLocalForRHS(((StmtMethodPosition) pos).getContainer(), (AssignStmt)stmt);

	return new SetFieldShadowMatch(pos.getContainer(),stmt,fr.getField());
    }
    /**
     * Ensures that the rhs of the set is a local.
     * Needed for around().
     * @param method
     * @param stmt
     */
    private static void makeLocalForRHS(SootMethod method, AssignStmt stmt) {
		Value val=stmt.getRightOp();
    	if (!(val instanceof Local)) {
			Body body=method.getActiveBody();
			Chain statements=body.getUnits().getNonPatchingChain();
			LocalGeneratorEx lg=new LocalGeneratorEx(body);
			
			Local l=lg.generateLocal(stmt.getLeftOp().getType(),
								"setRHSLocal");
			AssignStmt as=Jimple.v().newAssignStmt(l,val);
			statements.insertBefore(as, stmt);
			stmt.setRightOp(l);
			stmt.redirectJumpsToThisTo(as);
    	}
    }

    public AdviceApplication.SJPInfo makeSJPInfo() {
	// FIXME : dummy details string
	return new AdviceApplication.SJPInfo
	    ("field-set","FieldSignature","makeFieldSig",
	     "8-t-Test-Test-",stmt);
    }

    protected AdviceApplication doAddAdviceApplication
	(MethodAdviceList mal,AbstractAdviceDecl ad,Residue residue) {

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
