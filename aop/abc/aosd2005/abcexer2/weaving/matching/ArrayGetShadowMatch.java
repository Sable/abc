/*
 * Created on 09-Feb-2005
 */
package abcexer2.weaving.matching;

import java.util.ArrayList;
import java.util.List;

import polyglot.util.InternalCompilerError;

import soot.Immediate;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.NullConstant;
import soot.jimple.Stmt;
import soot.tagkit.Host;
import abc.eaj.weaving.matching.CastShadowMatch;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.MethodAdviceList;
import abc.weaving.matching.MethodPosition;
import abc.weaving.matching.SJPInfo;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.ShadowType;
import abc.weaving.matching.StmtAdviceApplication;
import abc.weaving.matching.StmtMethodPosition;
import abc.weaving.matching.StmtShadowMatch;
import abc.weaving.residues.ContextValue;
import abc.weaving.residues.JimpleValue;
import abc.weaving.residues.Residue;
import abc.weaving.weaver.ConstructorInliningMap;

/**
 * @author sascha
 *
 */
public class ArrayGetShadowMatch extends StmtShadowMatch {

	public ArrayGetShadowMatch(SootMethod container, Stmt stmt) {
		super(container, stmt);
	}
	
	public static ShadowType shadowType()
    {
        return new ShadowType() {
            public ShadowMatch matchesAt(MethodPosition pos) {
                return ArrayGetShadowMatch.matchesAt(pos);
            }
        };
    }
	
	public static ArrayGetShadowMatch matchesAt(MethodPosition pos)
    {
        if (!(pos instanceof StmtMethodPosition)) return null;
        if (abc.main.Debug.v().traceMatcher) System.err.println("ArrayGet");

        // In Jimple: * a cast can only appear as an expression
        //            * expressions are not recursive
        //            * expressions are only used as r-values
        //            * r-values only appear in assignments

        Stmt stmt = ((StmtMethodPosition) pos).getStmt();

        if (!(stmt instanceof AssignStmt)) return null;
        Value rhs = ((AssignStmt) stmt).getRightOp();

        if(!(rhs instanceof ArrayRef)) return null;
        ArrayRef ref=(ArrayRef)rhs;
        //ref.
        //Type cast_to = ((CastExpr) rhs).getCastType();

        return new ArrayGetShadowMatch(pos.getContainer(), stmt);
    }

	// TODO
	public ContextValue getReturningContextValue() {
        return new JimpleValue(NullConstant.v());
    }
	
	public List /*<ContextValue>*/ getArgsContextValues()
    {
        ArrayList ret = new ArrayList(1);

        ArrayRef ref = (ArrayRef) ((AssignStmt) stmt).getRightOp();
        ret.add(new JimpleValue((Immediate)ref.getIndex()));

        return ret;
    }

    public ContextValue getTargetContextValue()
    {
    	ArrayRef ref = (ArrayRef) ((AssignStmt) stmt).getRightOp();
    	
        return new JimpleValue((Immediate)ref.getBase());
    }

    // why is this necessary?
	public ShadowMatch inline(ConstructorInliningMap cim) {
        ShadowMatch ret = cim.map(this);
        if(ret != null) return ret;
        if( cim.inlinee() != container ) throw new InternalCompilerError(
                "inlinee "+cim.inlinee()+" doesn't match container "+container);
        ret = new ArrayGetShadowMatch(cim.target(), cim.map(stmt));
        cim.add(this, ret);
        return ret;
	}
	
    // why is this necessary?
	public Host getHost() {
		return stmt;
	}

	protected SJPInfo makeSJPInfo() {
		// TODO Auto-generated method stub
		return null;
	}

    // why is this necessary?
	protected AdviceApplication doAddAdviceApplication(MethodAdviceList mal,
			AbstractAdviceDecl ad, Residue residue) {
		StmtAdviceApplication aa = new StmtAdviceApplication(ad,residue,stmt);
        mal.addStmtAdvice(aa);
        return aa;
	}

	public String joinpointName() {
		return "arrayget";
	}
}
