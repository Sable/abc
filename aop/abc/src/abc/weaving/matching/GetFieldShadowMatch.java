package abc.weaving.matching;

import java.util.*;

import soot.*;
import soot.jimple.*;
import soot.tagkit.Host;

import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.MethodCategory;
import abc.weaving.residues.Residue;
import abc.weaving.residues.ContextValue;
import abc.weaving.residues.JimpleValue;

/** The results of matching at a field get
 *  @author Ganesh Sittampalam
 *  @author Oege de Moor
 */
public class GetFieldShadowMatch extends StmtShadowMatch {
    
    private SootFieldRef fieldref;
    
    private GetFieldShadowMatch(SootMethod container,Stmt stmt,SootFieldRef fieldref) {
	super(container,stmt);
	this.fieldref=fieldref;
    }

    public SootFieldRef getFieldRef() {
	return fieldref;
    }

    public static GetFieldShadowMatch matchesAt(MethodPosition pos) {
	if(!(pos instanceof StmtMethodPosition)) return null;
	if(abc.main.Debug.v().traceMatcher) System.err.println("GetField");

	Stmt stmt=((StmtMethodPosition) pos).getStmt();

	if (!(stmt instanceof AssignStmt)) return null;
	AssignStmt as = (AssignStmt) stmt;
	Value rhs = as.getRightOp();
       	if(rhs instanceof FieldRef) {
	    FieldRef fr = (FieldRef) rhs;
		if (MethodCategory.weaveSetGet(fr.getFieldRef()))
	    	return new GetFieldShadowMatch(pos.getContainer(),stmt,fr.getFieldRef());
	    else
	    	return null;
	} else if(rhs instanceof InvokeExpr) {
		InvokeExpr ie = (InvokeExpr) rhs;
		SootMethodRef smr = ie.getMethodRef();
	    if(MethodCategory.getCategory(smr)
	       ==MethodCategory.ACCESSOR_GET) {
			return new GetFieldShadowMatch
			    (pos.getContainer(),stmt,MethodCategory.getFieldRef(smr));
	    }
	    else return null;
	} else {
	    return null;
	}
    }

    public Host getHost() {
	return stmt;
    }
    
    public SJPInfo makeSJPInfo() {
	return new SJPInfo
	    ("field-get","FieldSignature","makeFieldSig",
	     SJPInfo.makeFieldSigData(container,fieldref.resolve()),stmt);
    }


    protected AdviceApplication doAddAdviceApplication
	(MethodAdviceList mal,AbstractAdviceDecl ad,Residue residue) {

	StmtAdviceApplication aa=new StmtAdviceApplication(ad,residue,stmt);
	mal.addStmtAdvice(aa);
	return aa;
    }

    public ContextValue getTargetContextValue() {
		if (stmt instanceof AssignStmt) {
			// System.out.println(stmt);
			AssignStmt a = (AssignStmt) stmt;
			Value rhs = a.getRightOp();
			if (rhs instanceof FieldRef) {
				FieldRef fr=(FieldRef) rhs;
				if(!(fr instanceof InstanceFieldRef)) return null;
				InstanceFieldRef ifr=(InstanceFieldRef) fr;
				return new JimpleValue(ifr.getBase());
			} else if (rhs instanceof InstanceInvokeExpr) {
				InstanceInvokeExpr vie = (InstanceInvokeExpr) rhs;
				if (MethodCategory.getCategory(vie.getMethodRef()) 
				    == MethodCategory.ACCESSOR_GET)
					return new JimpleValue(vie.getBase());
			} 
		} else if (stmt instanceof InvokeStmt) {
			InvokeExpr ie = ((InvokeStmt)stmt).getInvokeExpr();
			if (ie instanceof InstanceInvokeExpr) {
				InstanceInvokeExpr vie = (InstanceInvokeExpr) ie;
				if (MethodCategory.getCategory(vie.getMethodRef()) 
				    == MethodCategory.ACCESSOR_GET)
					return new JimpleValue(vie.getBase());
			}
		}
		return null;
    }

    public ContextValue getReturningContextValue() {
	return new JimpleValue(((AssignStmt) stmt).getLeftOp());
    }

    public List/*<ContextValue>*/ getArgsContextValues() {
	return new ArrayList(0);
    }

}
