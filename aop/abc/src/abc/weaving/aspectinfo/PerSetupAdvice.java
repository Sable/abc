package abc.weaving.aspectinfo;

import java.util.*;
import polyglot.util.Position;
import abc.weaving.matching.*;
import abc.weaving.weaver.WeavingContext;

public abstract class PerSetupAdvice extends AbstractAdviceDecl {

    private Aspect aspect;
    public Aspect getAspect() {
	return aspect;
    }

    protected PerSetupAdvice(Aspect aspect,Pointcut pc,Position pos) {
	super(new BeforeAdvice(pos),pc,new ArrayList(),pos);
	this.aspect=aspect;
    }

    public WeavingEnv getWeavingEnv() {
	return new EmptyFormals();
    }


}
