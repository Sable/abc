package abc.weaving.aspectinfo;

import java.util.*;
import polyglot.util.Position;
import soot.*;
import soot.jimple.*;
import soot.util.*;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.residues.*;
import abc.weaving.weaver.WeavingContext;
import abc.soot.util.LocalGeneratorEx;

public class PerCflowSetup extends PerSetupAdvice {

    private boolean isBelow;

    public PerCflowSetup(Aspect aspct,Pointcut pc,
			   boolean isBelow,Position pos) {
	super(new BeforeAfterAdvice(pos),aspct,pc,pos);
	this.isBelow=isBelow;
    }

    public boolean isBelow() {
	return isBelow;
    }

    public static class PerCflowSetupWeavingContext 
	extends WeavingContext
	implements BeforeAfterAdvice.ChoosePhase {

	public boolean doBefore;
	public void setBefore() { doBefore=true; }
	public void setAfter() { doBefore=false; }

    }

    public WeavingContext makeWeavingContext() {
	return new PerCflowSetupWeavingContext();
    }



    public Chain makeAdviceExecutionStmts
	 (AdviceApplication adviceappl,LocalGeneratorEx localgen,WeavingContext wc) {

	PerCflowSetupWeavingContext cswc=(PerCflowSetupWeavingContext) wc;

	if(cswc.doBefore) {

	    Chain c = new HashChain();
	    Type object=Scene.v().getSootClass("java.lang.Object").getType();
	    
	    SootClass aspectClass=getAspect().getInstanceClass().getSootClass();

	    SootMethodRef push=Scene.v().makeMethodRef
		(aspectClass,"abc$perCflowPush",new ArrayList(),VoidType.v());

	    c.addLast(Jimple.v().newInvokeStmt
		      (Jimple.v().newStaticInvokeExpr(push)));

	    return c;
	} else {
	    Chain c=new HashChain();
	    SootClass stackClass=Scene.v()
		.getSootClass("org.aspectj.runtime.internal.CFlowStack");
	    SootClass aspectClass=getAspect().getInstanceClass().getSootClass();

	    SootMethodRef pop=Scene.v().makeMethodRef(stackClass,"pop",new ArrayList(),VoidType.v());
	    SootFieldRef perCflowStackField
		=Scene.v().makeFieldRef(aspectClass,"abc$perCflowStack",stackClass.getType());

	    Local perCflowStackLoc=localgen.generateLocal(stackClass.getType(),"perCflowStack");
	    c.addLast(Jimple.v().newAssignStmt
		      (perCflowStackLoc,Jimple.v().newStaticFieldRef(perCflowStackField)));
	    c.addLast(Jimple.v().newInvokeStmt
		      (Jimple.v().newVirtualInvokeExpr(perCflowStackLoc,pop)));
	    return c;
	}
    }

    public Residue postResidue(ShadowMatch sm) {
	return AlwaysMatch.v();
    }


    public void debugInfo(String prefix,StringBuffer sb) {
	sb.append(prefix+" from aspect: "+getAspect().getName()+"\n");
	sb.append(prefix+" type: "+spec+"\n");
	sb.append(prefix+" pointcut: "+pc+"\n");
	sb.append(prefix+" special: percflow"+(isBelow?"below":"")+" instantiation\n");
    }
}
