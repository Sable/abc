package abc.weaving.residues;

import polyglot.util.InternalCompilerError;
import soot.*;
import soot.jimple.Jimple;
import abc.weaving.matching.AdviceApplication;

public class StaticJoinPointInfo extends ContextValue {

    private AdviceApplication.SJPInfo sjpInfo;

    public StaticJoinPointInfo(AdviceApplication.SJPInfo sjpInfo) {
	if(sjpInfo==null) 
	    throw new InternalCompilerError("StaticJoinPointInfo constructed with null argument");
	this.sjpInfo=sjpInfo;
    }

    public String toString() {
	return "thisJoinPointStaticPart";
    }

    public Type getSootType() {
	return RefType.v("org.aspectj.lang.JoinPoint$StaticPart");
    }

    public Value getSootValue() {
	return Jimple.v().newStaticFieldRef(sjpInfo.sjpfield);
    }
}
