package abc.weaving.matching;

import java.util.ArrayList;
import java.util.List;

import polyglot.util.InternalCompilerError;

import soot.*;
import soot.jimple.*;
import soot.tagkit.Host;
import soot.util.Chain;

import abc.soot.util.LocalGeneratorEx;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.residues.ContextValue;
import abc.weaving.residues.JimpleValue;
import abc.weaving.residues.Residue;
import abc.weaving.aspectinfo.MethodCategory;

/** The results of matching at a field set
 *  @author Ganesh Sittampalam
 *  @author Oege de Moor
 */
public class SetFieldShadowMatch extends StmtShadowMatch {
    
    private SootFieldRef fieldref;

    private SetFieldShadowMatch(SootMethod container,Stmt stmt,SootFieldRef fieldref) {
	super(container,stmt);
	this.fieldref=fieldref;
    }

    public SootFieldRef getFieldRef() {
	return fieldref;
    }

    public static SetFieldShadowMatch matchesAt(MethodPosition pos) {
	if(!(pos instanceof StmtMethodPosition)) return null;
	if(abc.main.Debug.v().traceMatcher) System.err.println("SetField");

	Stmt stmt=((StmtMethodPosition) pos).getStmt();
	
	SootFieldRef sfr = null;
	if(stmt instanceof AssignStmt) {
		AssignStmt as = (AssignStmt) stmt;
		Value lhs = as.getLeftOp();
		if (lhs instanceof FieldRef) {
			FieldRef fr = (FieldRef) lhs;
			sfr = fr.getFieldRef();
			if (!MethodCategory.weaveSetGet(sfr))
				return null;
			makeLocalForRHS(((StmtMethodPosition) pos).getContainer(), as);
		} else return null;
	} else if (stmt instanceof InvokeStmt) {
		InvokeStmt is = (InvokeStmt) stmt;
		// System.out.println("stmt="+stmt);
		InvokeExpr ie = is.getInvokeExpr();
		SootMethodRef smr = ie.getMethodRef();
		if(MethodCategory.getCategory(smr)
				   ==MethodCategory.ACCESSOR_SET) {
					sfr = MethodCategory.getFieldRef(smr);
					// FIXME: make local for argument?
		}
		else return null;
	} else return null;
     
	

	return new SetFieldShadowMatch(pos.getContainer(),stmt,sfr);
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
    

    public Host getHost() {
	return stmt;
    }
    
    public SJPInfo makeSJPInfo() {
	return new SJPInfo
	    ("field-set","FieldSignature","makeFieldSig",
	     SJPInfo.makeFieldSigData(container,fieldref.resolve()),stmt);
    }

    protected AdviceApplication doAddAdviceApplication
	(MethodAdviceList mal,AbstractAdviceDecl ad,Residue residue) {

	StmtAdviceApplication aa=new StmtAdviceApplication(ad,residue,stmt);
	mal.addStmtAdvice(aa);
	return aa;
    }

       
	public ContextValue getTargetContextValue() {
		// System.out.println(stmt);
		if (stmt instanceof AssignStmt) {
			// System.out.println(stmt);
			AssignStmt a = (AssignStmt) stmt;
			Value lhs = a.getLeftOp();
			if (lhs instanceof FieldRef) {
				FieldRef fr=(FieldRef) lhs;
				if(!(fr instanceof InstanceFieldRef)) return null;
				InstanceFieldRef ifr=(InstanceFieldRef) fr;
				return new JimpleValue(ifr.getBase());
			}
			Value rhs = a.getRightOp();
			if (rhs instanceof InvokeExpr) {
			InstanceInvokeExpr vie = (InstanceInvokeExpr) rhs;
			if (MethodCategory.getCategory(vie.getMethodRef()) 
			    == MethodCategory.ACCESSOR_SET)
				return new JimpleValue(vie.getBase());
		}
		} else if (stmt instanceof InvokeStmt) {
			InvokeExpr ie = ((InvokeStmt)stmt).getInvokeExpr();
			if (ie instanceof InstanceInvokeExpr) {
				InstanceInvokeExpr vie = (InstanceInvokeExpr) ie;
				if (MethodCategory.getCategory(vie.getMethodRef()) 
				    == MethodCategory.ACCESSOR_SET)
					return new JimpleValue(vie.getBase());
			}

		}
		return null;
	}

    public List/*<ContextValue>*/ getArgsContextValues() {
	ArrayList ret=new ArrayList(1);
	if (stmt instanceof AssignStmt) {
		AssignStmt a = (AssignStmt) stmt;
		if (a.getLeftOp() instanceof FieldRef)
			ret.add(new JimpleValue(((AssignStmt) stmt).getRightOp()));
		Value rhs = a.getRightOp();
		if (rhs instanceof InvokeExpr) {
			InvokeExpr vie = (InvokeExpr) rhs;
			if (MethodCategory.getCategory(vie.getMethodRef()) 
			    == MethodCategory.ACCESSOR_SET)
				ret.add(new JimpleValue(vie.getArg(0)));
		}
	} else if (stmt instanceof InvokeStmt) {
		InvokeExpr ie = ((InvokeStmt)stmt).getInvokeExpr();
		ret.add(new JimpleValue(ie.getArg(0)));
	} else throw new InternalCompilerError("stmt neither an assignment nor an invoke");
	return ret;
    }

    public String joinpointName() {
	return "field set";
    }



}
