package abc.weaving.aspectinfo;

import java.util.*;
import polyglot.util.Position;
import polyglot.util.InternalCompilerError;
import soot.*;
import soot.jimple.*;
import soot.util.*;
import abc.weaving.matching.*;
import abc.weaving.residues.*;
import abc.weaving.weaver.AspectCodeGen;
import abc.weaving.weaver.WeavingContext;
import abc.soot.util.LocalGeneratorEx;
import abc.soot.util.Restructure;

public class CflowSetup extends AbstractAdviceDecl {

    public static CflowSetup construct(Aspect aspect,
				       Pointcut pc,
				       boolean isBelow,
				       Hashtable typeMap,
				       Position pos) {
	// this returns a set of String rather than Var because
	// each occurrence will have a different position
	Set/*<String>*/ fvs=new HashSet();
	pc.getFreeVars(fvs);
	List formals=new LinkedList();
	List actuals=new LinkedList();
	Iterator it=fvs.iterator();
	// FIXME make all the formals have type Object
	while(it.hasNext()) {
	    String fv=(String) (it.next());
	    AbcType type=(AbcType) typeMap.get(fv);
	    Formal f=new Formal(type,fv,pos);
	    formals.add(f);
	    actuals.add(new Var(fv,pos));
	}

	return new CflowSetup(aspect,pc,isBelow,formals,actuals,pos);
    }

    private boolean isBelow;

    private List/*<Var>*/ actuals;

    private CflowSetup(Aspect aspect,Pointcut pc,boolean isBelow,
		       List/*<Formal>*/ formals,List/*<Var>*/ actuals,
		       Position pos) {
	super(aspect,new BeforeAfterAdvice(pos),pc,formals,pos);
	this.actuals=actuals;
	this.isBelow=isBelow;
    }

    public boolean isBelow() {
	return isBelow;
    }

    public List/*<Var>*/ getActuals() {
	return actuals;
    }

    public void debugInfo(String prefix,StringBuffer sb) {
	sb.append(prefix+" type: "+spec+"\n");
	sb.append(prefix+" pointcut: "+pc+"\n");
	sb.append(prefix+" special: "+(isBelow ? "cflowbelow" : "cflow")
		  +" setup\n");
    }

    public static class CflowSetupWeavingContext 
	extends WeavingContext
	implements BeforeAfterAdvice.ChoosePhase {

	public Vector/*<Value>*/ bounds;
	public boolean doBefore;
	public void setBefore() { doBefore=true; }
	public void setAfter() { doBefore=false; }

	public CflowSetupWeavingContext(int boundsize) {
	    bounds=new Vector(boundsize);
	    bounds.setSize(boundsize);
	}
    }

    public static class CflowSetupBound extends WeavingVar {
	private int pos;
	private Type type;
	private boolean mustbox;
	private Local loc=null;

	public CflowSetupBound(int pos,Type type,boolean mustbox) {
	    this.pos=pos;
	    this.type=type;
	    this.mustbox=mustbox;
	}

	public String toString() {
	    return "cflowbound("+pos+":"+type+")";
	}

	public Stmt set(LocalGeneratorEx localgen,Chain units,Stmt begin,
			WeavingContext wc,Value val) {
	    if(loc==null) loc = localgen.generateLocal(type,"adviceformal");
	    Stmt assignStmt=Jimple.v().newAssignStmt(loc,val);
	    units.insertAfter(assignStmt,begin);
	    ((CflowSetupWeavingContext) wc).bounds.setElementAt(loc,pos);
	    return assignStmt;
	}
	
	public Local get() {
	    if(loc==null) 
		throw new RuntimeException
		    ("Internal error: someone tried to read from a variable bound "
		     +"to a cflow advice formal before it was written");
	    
	    return loc;
	}
	
	public boolean hasType() {
	    return true;
	}
	
	public Type getType() {
	    return type;
	}
	
	public boolean mustBox() {
	    return mustbox;
	}
	
    }

    // not static deliberately
    public class CflowBoundVars implements WeavingEnv {
	public CflowBoundVars() {
	}

	private Hashtable setupbounds=new Hashtable();

	public WeavingVar getWeavingVar(Var v) {
	    if(setupbounds.containsKey(v.getName())) 
		return (CflowSetupBound) setupbounds.get(v.getName());

	    int index=-1; Type type=null;
	    int i=0;
	    Iterator it=getFormals().iterator();
	    while(it.hasNext()) {
		Formal f=(Formal) (it.next());
		if(f.getName().equals(v.getName())) {
		    index=i;
		    type=f.getType().getSootType();
		}
		i++;
	    }
	    if(index==-1) 
		throw new InternalCompilerError
		    ("Variable "+v.getName()+" not found in context",
		     v.getPosition());

	    boolean mustbox;
	    if(type instanceof PrimType) {
		type=Restructure.JavaTypeInfo.getBoxingClass(type).getType();
		mustbox=true;
	    } else mustbox=false;

	    CflowSetupBound setupbound=new CflowSetupBound(index,type,mustbox);

	    setupbounds.put(v.getName(),setupbound);
	    return setupbound;
	}

	public AbcType getAbcType(Var v) {
	    AbcType type=null;
	    Iterator it=getFormals().iterator();
	    while(it.hasNext()) {
		Formal f=(Formal) (it.next());
		if(f.getName().equals(v.getName()))
		    type=f.getType();
	    }
	    if(type==null) 
		throw new InternalCompilerError
		    ("Variable "+v.getName()+" not found in context",
		     v.getPosition());
	    /*
	      if(type.getSootType() instanceof PrimType)
	      type=new AbcType(Restructure.JavaTypeInfo.getBoxingClass
	      (type.getSootType()).getType());
	    */
	    return type;
	}
    }

    public WeavingEnv getWeavingEnv() {
	return new CflowBoundVars();
    }

    public WeavingContext makeWeavingContext() {
	return new CflowSetupWeavingContext(getFormals().size());
    }

    private SootField cflowStack=null;
    public SootField getCflowStack() {
	if(cflowStack==null) {
	    SootClass cl=getAspect().getInstanceClass().getSootClass();

	    int i=0;
	    String name;
	    do {
		name="abc$cflowStack$"+i;
		i++;
	    } while(cl.declaresFieldByName(name));
	    
	    SootClass stackClass=Scene.v()
		.loadClassAndSupport("org.aspectj.runtime.internal.CFlowStack");
	    RefType stackType=stackClass.getType();

	    cflowStack=new SootField(name,stackType,
				     Modifier.PUBLIC | Modifier.STATIC);

	    SootMethod preClinit=new AspectCodeGen().getPreClinit(cl);
	    LocalGeneratorEx lg=new LocalGeneratorEx
		(preClinit.getActiveBody());
	    
	    Local loc=lg.generateLocal(stackType,"cflowStack$"+i);
	    Chain units=preClinit.getActiveBody().getUnits();

	    Stmt returnStmt=(Stmt) units.getFirst();
	    while(!(returnStmt instanceof ReturnVoidStmt)) 
		returnStmt=(Stmt) units.getSuccOf(returnStmt);
	    
	    units.insertBefore(Jimple.v().newAssignStmt
			       (loc,Jimple.v().newNewExpr(stackType)),
			       returnStmt);
	    units.insertBefore(Jimple.v().newInvokeStmt
			       (Jimple.v().newSpecialInvokeExpr
				(loc,stackClass.getMethod
				 (SootMethod.constructorName,
				  new ArrayList()))),
			       returnStmt);
	    units.insertBefore(Jimple.v().newAssignStmt
			       (Jimple.v().newStaticFieldRef(cflowStack),loc),
			       returnStmt);
	    cl.addField(cflowStack);
	}
	return cflowStack;
    }

    public Chain makeAdviceExecutionStmts
	 (LocalGeneratorEx localgen,WeavingContext wc) {

	CflowSetupWeavingContext cswc=(CflowSetupWeavingContext) wc;

	if(cswc.doBefore) {

	    Chain c = new HashChain();
	    Type object=Scene.v().getSootClass("java.lang.Object").getType();
	    
	    Local l=localgen.generateLocal
		(ArrayType.v(object,1),"cflowbounds");

	    c.addLast
		(Jimple.v().newAssignStmt
		 (l,Jimple.v().newNewArrayExpr(object,IntConstant.v(getFormals().size()))));

	    for(int i=0;i<getFormals().size();i++)
		c.addLast(Jimple.v().newAssignStmt
			  (Jimple.v().newArrayRef(l,IntConstant.v(i)),
			   (Value) (cswc.bounds.get(i))));


	    SootClass stackClass=Scene.v()
		.loadClassAndSupport("org.aspectj.runtime.internal.CFlowStack");

	    ArrayList types=new ArrayList(1);
	    types.add(ArrayType.v(object,1));
	    SootMethod push=stackClass.getMethod("push",types);

	    Local cflowStack=localgen.generateLocal(stackClass.getType(),"cflowstack");
	    c.addLast(Jimple.v().newAssignStmt
		      (cflowStack,Jimple.v().newStaticFieldRef(getCflowStack())));

	    c.addLast(Jimple.v().newInvokeStmt
		      (Jimple.v().newVirtualInvokeExpr(cflowStack,push,l)));

	    return c;
	} else {
	    Chain c=new HashChain();
	    SootClass stackClass=Scene.v()
		.loadClassAndSupport("org.aspectj.runtime.internal.CFlowStack");
	    SootMethod pop=stackClass.getMethod("pop",new ArrayList());
	    Local cflowStack=localgen.generateLocal(stackClass.getType(),"cflowstack");
	    c.addLast(Jimple.v().newAssignStmt
		      (cflowStack,Jimple.v().newStaticFieldRef(getCflowStack())));
	    c.addLast(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(cflowStack,pop)));
	    return c;
	}
    }

    public static int getPrecedence(CflowSetup a,CflowSetup b) {
	// We know that the belows are the same and that we are in
	// the same aspect

	// FIXME: Best guess is to compare by positions, but is this correct w.r.t inlining?
	if(a.getPosition().line() < b.getPosition().line()) 
	    return GlobalAspectInfo.PRECEDENCE_FIRST;
	if(a.getPosition().line() > b.getPosition().line()) 
	    return GlobalAspectInfo.PRECEDENCE_SECOND;

	if(a.getPosition().column() < b.getPosition().column()) 
	    return GlobalAspectInfo.PRECEDENCE_FIRST;
	if(a.getPosition().column() > b.getPosition().column()) 
	    return GlobalAspectInfo.PRECEDENCE_SECOND;

	// Trying to compare the same advice, I guess... (modulo inlining behaviour)
	return GlobalAspectInfo.PRECEDENCE_NONE;

    }

}
