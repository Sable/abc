package abc.weaving.aspectinfo;

import java.util.*;
import polyglot.util.Position;
import soot.*;
import soot.jimple.Jimple;
import soot.util.*;
import abc.weaving.matching.*;
import abc.weaving.residues.*;
import abc.weaving.weaver.WeavingContext;
import abc.weaving.weaver.SingleValueWeavingContext;
import abc.soot.util.LocalGeneratorEx;

public class PerTargetSetup extends PerSetupAdvice {

    protected PerTargetSetup(Aspect aspct,Pointcut pc,Position pos) {
	super(new BeforeAdvice(pos),aspct,pc,pos);
    }

    public WeavingContext makeWeavingContext() {
	return new SingleValueWeavingContext();
    }

    public Chain makeAdviceExecutionStmts
	(AdviceApplication adviceappl,LocalGeneratorEx localgen,WeavingContext wc) {

	SingleValueWeavingContext svwc=(SingleValueWeavingContext) wc;
	SootClass aspectclass=getAspect().getInstanceClass().getSootClass();

	Chain c=new HashChain();

	List paramTypes=new ArrayList(1);
	paramTypes.add(Scene.v().getSootClass("java.lang.Object").getType());

	c.addLast(Jimple.v().newInvokeStmt
		  (Jimple.v().newStaticInvokeExpr
		   (Scene.v().makeMethodRef(aspectclass,"abc$perTargetBind",paramTypes,VoidType.v()),
		    svwc.value)));

	return c;

    }

    public Residue postResidue(ShadowMatch sm) {
	Type type=Scene.v().getSootClass("java.lang.Object").getType();
	ContextValue targetcv=sm.getTargetContextValue();
	if(targetcv==null) return null;
	return Bind.construct(targetcv,type,new SingleValueVar(type));
    }

    public void debugInfo(String prefix,StringBuffer sb) {
	sb.append(prefix+" from aspect: "+getAspect().getName()+"\n");
	sb.append(prefix+" type: "+spec+"\n");
	sb.append(prefix+" pointcut: "+pc+"\n");
	sb.append(prefix+" special: pertarget instantiation\n");
    }

    
}
