package abc.weaving.aspectinfo;

import java.util.*;
import polyglot.util.Position;
import soot.*;
import soot.jimple.*;
import soot.util.*;
import abc.weaving.matching.*;
import abc.weaving.weaver.WeavingContext;
import abc.weaving.weaver.SingleValueWeavingContext;
import abc.soot.util.LocalGeneratorEx;

/** A <code>declare soft</code> declaration. */
public class DeclareSoft extends AbstractAdviceDecl {
    private AbcType exc;
    private Pointcut pc;
    private Aspect aspct;

    public DeclareSoft(AbcType exc, Pointcut pc, Aspect aspct, Position pos) {
	super(new SoftenAdvice((RefType) exc.getSootType(),pos),
	      pc, new ArrayList(), pos);
	this.aspct=aspct;
	this.exc = exc;
    }

    // static because otherwise we can't use it in the constructor call of the super class
    public static class SoftenAdvice extends AfterThrowingAdvice {
	RefType exc;

	public SoftenAdvice(RefType exc, Position pos) {
	    super(pos);
	    this.exc=exc;
	}

	public String toString() {
	    return "soften exception";
	}

	// We inherit the matchesAt method from AfterThrowingAdvice,
	// because the binding of the formal is best done as a special
	// case in the weaver for after throwing advice

	public RefType getCatchType() {
	    return exc;
	}

	public void bindException(WeavingContext wc,AbstractAdviceDecl ad,Local exception) {
	    ((SingleValueWeavingContext) wc).value=exception;
	}
    }

    /** Get the softened exception. */
    public AbcType getException() {
	return exc;
    }

    public Aspect getAspect() {
	return aspct;
    }

    public void debugInfo(String prefix,StringBuffer sb) {
	sb.append(prefix+" from aspect: "+getAspect().getName()+"\n");
	sb.append(prefix+" exception: "+exc+"\n");
	sb.append(prefix+" pointcut: "+pc+"\n");
	sb.append(prefix+" special: declare soft\n");
    }

    public WeavingEnv getWeavingEnv() {
	return new EmptyFormals();
    }

    public WeavingContext makeWeavingContext() {
	return new SingleValueWeavingContext();
    }

    public Chain makeAdviceExecutionStmts
	(LocalGeneratorEx localgen,WeavingContext wc) {

	Chain units=new HashChain();

	SootClass soft=Scene.v().loadClassAndSupport("org.aspectj.lang.SoftException");

	Value ex=((SingleValueWeavingContext) wc).value;
	Local softexc=localgen.generateLocal(soft.getType(),"softexception");

	units.addLast(Jimple.v().newAssignStmt(softexc,
					       Jimple.v().newNewExpr(soft.getType())));

	List argsTypeList=new ArrayList(1);
	argsTypeList.add(RefType.v("java.lang.Throwable"));
	SootMethod constr=soft.getMethod(SootMethod.constructorName,argsTypeList);
	units.addLast(Jimple.v().newInvokeStmt
		      (Jimple.v().newSpecialInvokeExpr(softexc,constr,ex)));


	units.addLast(Jimple.v().newThrowStmt(softexc));

	return units;

    }

    public String toString() {
	return "soften "+getException()+" at "+getPointcut();
    }
}
