/*
 * Created on 09-Feb-2005
 */
package abcexer2.weaving.matching;

import java.util.ArrayList;
import java.util.List;

import polyglot.util.InternalCompilerError;
import soot.Body;
import soot.Immediate;
import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.tagkit.Host;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;
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
 * @author Sascha Kuzins
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
        	//FIXME: add and test this: stmt.redirectJumpsToThisTo(as);
        	ref.setIndex(l);
        }
        
        return new ArrayGetShadowMatch(pos.getContainer(), stmt);
    }

	// Set the left hand side of the assignment as the joinpoint return value.
	// This is always a local.
	// is this correct?
	public ContextValue getReturningContextValue() {
        return new JimpleValue( (Immediate)  (Local) ((AssignStmt) stmt).getLeftOp()  );
    }
	
	// set the index of the array access as the joinpoint argument
	public List /*<ContextValue>*/ getArgsContextValues()
    {
        ArrayList ret = new ArrayList(1);

        ArrayRef ref = (ArrayRef) ((AssignStmt) stmt).getRightOp();
        ret.add(new JimpleValue((Immediate)ref.getIndex()));

        return ret;
    }

	// set the array itself as the target
    public ContextValue getTargetContextValue()
    {
    	ArrayRef ref = (ArrayRef) ((AssignStmt) stmt).getRightOp();
        return new JimpleValue((Immediate)ref.getBase());
    }

    // could we provide some default implementation?
	public ShadowMatch inline(ConstructorInliningMap cim) {
        ShadowMatch ret = cim.map(this);
        if(ret != null) return ret;
        if( cim.inlinee() != container ) throw new InternalCompilerError(
                "inlinee "+cim.inlinee()+" doesn't match container "+container);
        ret = new ArrayGetShadowMatch(cim.target(), cim.map(stmt));
        cim.add(this, ret);
        return ret;
	}
	
	public SJPInfo makeSJPInfo()
    {
        return abc.main.Main.v().getAbcExtension().createSJPInfo
          ("arrayget",
           "abcexer2.lang.reflect.ArrayGetSignature",
           "makeArrayGetSig",
           ExtendedSJPInfo.makeArrayGetSigData(container), stmt);
    }


	public String joinpointName() {
		return "arrayget";
	}
}
