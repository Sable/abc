package abc.weaving.aspectinfo;

import java.util.*;
import polyglot.util.Position;
import abc.weaving.matching.*;
import abc.weaving.weaver.WeavingContext;

public abstract class PerSetupAdvice extends AbstractAdviceDecl {

    private Aspect aspct;
    public Aspect getAspect() {
	return aspct;
    }

    protected PerSetupAdvice(AdviceSpec spec,Aspect aspct,Pointcut pc,Position pos) {
	super(spec,pc,new ArrayList(),pos);
	this.aspct=aspct;
    }

    public WeavingEnv getWeavingEnv() {
	return new EmptyFormals();
    }

    public String toString() {
	return "setup advice for "+getAspect();
    }


}
