package abc.weaving.residues;

import java.util.*;
import soot.*;
import soot.util.Chain;
import soot.jimple.*;
import polyglot.util.InternalCompilerError;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;
import abc.weaving.weaver.AdviceWeavingContext;

/** A residue that puts the relevant aspect instance into a 
 * local variable in the weaving context
 *  @author Ganesh Sittampalam
 */ 

public class AspectOf extends Residue {

    private SootClass aspct;

    // null to indicate singleton aspect; i.e. no params to aspectOf
    private ContextValue pervalue;

    public AspectOf(SootClass aspct,ContextValue pervalue) {
	this.aspct=aspct;
	this.pervalue=pervalue;
    }

    public Stmt codeGen(SootMethod method,LocalGeneratorEx localgen,
			Chain units,Stmt begin,Stmt fail,boolean sense,
			WeavingContext wc) {

	// We don't expect the frontend/matcher to produce a residue that does this. 
	// There's no reason we couldn't just do the standard "automatic fail" thing 
	// if there was ever a need, though.
	if(!sense) 
	    throw new InternalCompilerError("aspectOf residue should never be used negated");

       	List paramTypes;
	List params;
	if(pervalue==null) {
	    params=new ArrayList(); paramTypes=new ArrayList();
	} else {
	    params=new ArrayList(1); paramTypes=new ArrayList(1);
	    paramTypes.add(Scene.v().getSootClass("java.lang.Object").getType());
	    params.add(pervalue.getSootValue());
	}
	
	Local aspectref = localgen.generateLocal(aspct.getType(),"theAspect");

	AssignStmt stmtAspectOf = Jimple.v().newAssignStmt
	    (aspectref, Jimple.v().newStaticInvokeExpr
	     (Scene.v().makeMethodRef(aspct,"aspectOf",paramTypes,aspct.getType(),true),params));

	units.insertAfter(stmtAspectOf,begin);
	((AdviceWeavingContext) wc).aspectinstance=aspectref;
	return stmtAspectOf;
    }

    public String toString() {
	return "aspectof("+aspct+","+pervalue+")";
    }

}
