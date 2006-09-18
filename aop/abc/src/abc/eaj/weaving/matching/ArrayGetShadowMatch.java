package abc.eaj.weaving.matching;

import soot.*;
import soot.jimple.*;
import soot.util.*;
import abc.soot.util.*;
import abc.weaving.matching.*;
import abc.weaving.residues.*;
import abc.weaving.weaver.*;
import java.util.*;
import polyglot.util.InternalCompilerError;

/**
 * @author Pavel Avgustinov
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
    
    public static ArrayGetShadowMatch matchesAt(MethodPosition pos) {
        if (!(pos instanceof StmtMethodPosition)) return null;
        if (abc.main.Debug.v().traceMatcher) System.err.println("ArrayGet");

        // In Jimple: * an arrayref can only appear as an expression
        //            * expressions are not recursive
        //            * expressions are only used as r-values
        //            * r-values only appear in assignments

        Stmt stmt = ((StmtMethodPosition) pos).getStmt();

        if (!(stmt instanceof AssignStmt)) return null;
        AssignStmt assign=(AssignStmt)stmt;
        Value rhs = assign.getRightOp();

        if(!(rhs instanceof ArrayRef)) return null;
        ArrayRef ref=(ArrayRef)rhs;

        Value index=ref.getIndex();
        // make sure the index is a local.
        // restructure if necessary.
        if (!(index instanceof Local)) {
            Body body=pos.getContainer().getActiveBody();
            Chain statements=body.getUnits().getNonPatchingChain();
            LocalGeneratorEx lg=new LocalGeneratorEx(body);
            Local l=lg.generateLocal(index.getType());
            AssignStmt as=Jimple.v().newAssignStmt(l, index);
            statements.insertBefore(as,stmt);
            stmt.redirectJumpsToThisTo(as);
            ref.setIndex(l);
        }

        return new ArrayGetShadowMatch(pos.getContainer(), stmt);
    }
    
    // Set the LHS of the assignment as the joinpoint return value. This is always a local.
    public ContextValue getReturningContextValue() {
    	return new JimpleValue((Immediate) ((AssignStmt)stmt).getLeftOp());
    }
    
    // set the index of the array access as the joinpoint argument
    public List getArgsContextValues() {
    	ArrayList ret = new ArrayList(1);
    	
    	ArrayRef ref = (ArrayRef) ((AssignStmt)stmt).getRightOp();
    	ret.add(new JimpleValue((Immediate)ref.getIndex()));
    	
    	return ret;
    }

    // set the array itself as the target
	public ContextValue getTargetContextValue() {
		ArrayRef ref = (ArrayRef) ((AssignStmt) stmt).getRightOp();
		return new JimpleValue((Immediate)ref.getBase());
	}

	public ShadowMatch inline(ConstructorInliningMap cim) {
		ShadowMatch ret = cim.map(this);
		if(ret != null) return ret;
		if(cim.inlinee() != container) throw new InternalCompilerError("inlinee " + cim.inlinee() + 
				"doesn't match container " + container);
		ret = new ArrayGetShadowMatch(cim.target(), cim.map(stmt));
		cim.add(this, ret);
		return ret;
	}

	public String joinpointName() {
		return "arrayget";
	}

	protected SJPInfo makeSJPInfo() {
		return abc.main.Main.v().getAbcExtension().createSJPInfo("arrayget",
				"org.aspectbench.eaj.lang.reflect.ArrayGetSignature", "makeArrayGetSig", 
				ExtendedSJPInfo.makeArrayGetSigData(container), stmt);
	}

}
