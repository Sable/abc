package abc.weaving.aspectinfo;

import java.util.*;
import soot.*;
import soot.jimple.*;
import soot.util.*;
import polyglot.util.Position;
import polyglot.util.InternalCompilerError;
import polyglot.util.ErrorQueue;
import polyglot.util.ErrorInfo;
import abc.weaving.matching.*;
import abc.weaving.residues.*;
import abc.weaving.weaver.WeavingContext;
import abc.weaving.weaver.AdviceWeavingContext;
import abc.weaving.weaver.PointcutCodeGen;
import abc.weaving.weaver.CodeGenException;
import abc.polyglot.util.ErrorInfoFactory;
import abc.soot.util.LocalGeneratorEx;

/** An advice declaration. */
public class AdviceDecl extends AbstractAdviceDecl {

    private MethodSig impl;
    private int jp,jpsp,ejp;

    private int nformals; // the number of formals in the advice implementation


    private Map/*<String,Integer>*/ formal_pos_map = new HashMap();
    private Map/*<String,AbcType>*/ formal_type_map = new HashMap();
    private List/*<MethodSig>*/ proceeds;

    public AdviceDecl(AdviceSpec spec, Pointcut pc, MethodSig impl, Aspect aspct, 
		      int jp, int jpsp, int ejp, List proceeds, Position pos) {

	super(aspct,spec,pc,impl.getFormals(),pos);
	this.impl = impl;
	this.jp = jp;
	this.jpsp = jpsp;
	this.ejp = ejp;
	this.proceeds = proceeds;

	int i = 0;
	nformals = impl.getFormals().size();
	Iterator fi = impl.getFormals().iterator();
	while (fi.hasNext()) {
	    Formal f = (Formal)fi.next();
	    formal_pos_map.put(f.getName(), new Integer(i++));
	    formal_type_map.put(f.getName(),f.getType());
	}
    }

    public int getFormalIndex(String name) {
	Integer i = (Integer)formal_pos_map.get(name);
	if (i == null) {
	    throw new RuntimeException("Advice formal "+name+" not found");
	}
	return i.intValue();
    }

    public AbcType getFormalType(String name) {
	AbcType t = (AbcType)formal_type_map.get(name);
	if(t==null) {
	    throw new RuntimeException("Advice formal "+name+" not found");
	}
	return t;
    }
	



    /** Get the signature of the placeholder method that contains the
     *  body of this advice.
     */
    public MethodSig getImpl() {
	return impl;
    }

    public boolean hasJoinPoint() {
	return jp != -1;
    }

    public boolean hasJoinPointStaticPart() {
	return jpsp != -1;
    }

    public boolean hasEnclosingJoinPoint() {
	return ejp != -1;
    }

    public int joinPointPos() {
	return jp;
    }

    public int joinPointStaticPartPos() {
	return jpsp;
    }

    public int enclosingJoinPointPos() {
	return ejp;
    }

    /** return number of formals (useful for determining number of args
     *     for invokes in code generator)
     */
    // Inline and delete?
    public int numFormals() {
         return nformals;
    }

    public Residue preResidue(ShadowMatch sm) {
	return getAspect().getPer().matchesAt(getAspect(),sm);
    }
	
    public Residue postResidue(ShadowMatch sm) {
	List/*<SootClass>*/ advicethrown
	    =getImpl().getSootMethod().getExceptions();

	List/*<SootClass>*/ shadowthrown
	    =sm.getExceptions();
	
	eachadvicethrow:
	for(Iterator advicethrownit=advicethrown.iterator();
	    advicethrownit.hasNext();
	    ) {
	    SootClass advicethrow=(SootClass) (advicethrownit.next());

	    for(Iterator shadowthrownit=shadowthrown.iterator();
		shadowthrownit.hasNext();
		) {

		SootClass shadowthrow=(SootClass) (shadowthrownit.next());
		if(Scene.v().getOrMakeFastHierarchy().isSubclass(advicethrow,shadowthrow))
		    break eachadvicethrow;
	    }

	    // FIXME: this should be a multi-position error
	    abc.main.Main.v().error_queue.enqueue
		(ErrorInfoFactory.newErrorInfo
		 (ErrorInfo.SEMANTIC_ERROR,
		  "Advice from aspect "
		  +getAspect().getInstanceClass().getSootClass()
		  +" applies here, and throws exception "+advicethrow
		  +" which is not already thrown here",
		  sm.getContainer(),
		  sm.getHost()));

	}

	Residue ret=AlwaysMatch.v;

	// cache the residue in the SJPInfo to avoid multiple field gets?
	// (could do this in the same place we get the JP stuff if we care)

	if(hasJoinPointStaticPart()) 
	    ret=AndResidue.construct
		(ret,new Load
		 (new StaticJoinPointInfo(sm.getSJPInfo()),
		  new AdviceFormal
		  (joinPointStaticPartPos(),
		   RefType.v("org.aspectj.lang.JoinPoint$StaticPart"))));
		   

	if(hasEnclosingJoinPoint()) 
	    ret=AndResidue.construct
		(ret,new Load
		 (new StaticJoinPointInfo(sm.getEnclosing().getSJPInfo()),
		  new AdviceFormal
		  (enclosingJoinPointPos(),
		   RefType.v("org.aspectj.lang.JoinPoint$StaticPart"))));
		   
	if(hasJoinPoint()) {
	    ret=AndResidue.construct
		(ret,new Load
		 (new JoinPointInfo(sm),
		  new AdviceFormal
		  (joinPointPos(),
		   RefType.v("org.aspectj.lang.JoinPoint"))));
	    // make sure the SJP info will be around later for 
	    // the JoinPointInfo residue
	    sm.recordSJPInfo(); 
	}

	ret=AndResidue.construct
	    (ret,getAspect().getPer().getAspectInstance(getAspect(),sm));
	return ret;

    }
    

    public WeavingContext makeWeavingContext() {

	int nformals = numFormals();
	PointcutCodeGen.debug("There are " + nformals + " formals to the advice method.");
	Vector arglist = new Vector(nformals, 2);
	arglist.setSize(nformals);
	return new AdviceWeavingContext(arglist);
    }


 
    /** create the invoke to call the advice body */
    public Chain makeAdviceExecutionStmts
	(LocalGeneratorEx localgen,WeavingContext wc) {

	Chain c = new HashChain();

	AdviceWeavingContext awc=(AdviceWeavingContext) wc;

	SootMethod advicemethod = getImpl().getSootMethod();

	for (int i = 0; i < awc.arglist.size(); i++)
	    if(awc.arglist.get(i)==null)
		throw new InternalCompilerError
		    ("Formal "+i+" to advice "+advicemethod.getSignature()+" not filled in",getPosition());

	Stmt s =Jimple.v().newInvokeStmt
	    (Jimple.v().newVirtualInvokeExpr
	     (awc.aspectinstance,advicemethod,awc.arglist)
	     );
	c.addLast(s);
	return (c);
	    
    }


    public String toString() {
	return "(in aspect "+getAspect().getName()+") "+spec+": "+pc+" >> "+impl+" <<"
	    +(hasJoinPoint() ? " thisJoinPoint" : "")
	    +(hasJoinPointStaticPart() ? " thisJoinPointStaticPart" : "")
	    +(hasEnclosingJoinPoint() ? " thisEnclosingJoinPoint" : "");
    }

    public void debugInfo(String prefix,StringBuffer sb) {
	sb.append(prefix+" in aspect: "+getAspect().getName()+"\n");
	sb.append(prefix+" type: "+spec+"\n");
	sb.append(prefix+" pointcut: "+pc+"\n");
	sb.append(prefix+" implementation: "+impl+"\n");
    }

    public WeavingEnv getWeavingEnv() {
	// FIXME: cache this?
	return new AdviceFormals(this);
    }

    public static int getPrecedence(AdviceDecl a,AdviceDecl b) {
	// We know that we are in the same aspect

	int lexicalfirst,lexicalsecond;

	if(a.getAdviceSpec().isAfter() || b.getAdviceSpec().isAfter()) {
	    lexicalfirst=GlobalAspectInfo.PRECEDENCE_SECOND;
	    lexicalsecond=GlobalAspectInfo.PRECEDENCE_FIRST;
	} else {
	    lexicalfirst=GlobalAspectInfo.PRECEDENCE_FIRST;
	    lexicalsecond=GlobalAspectInfo.PRECEDENCE_SECOND;
	}

	if(a.getPosition().line() < b.getPosition().line()) 
	    return lexicalfirst;
	if(a.getPosition().line() > b.getPosition().line()) 
	    return lexicalsecond;

	if(a.getPosition().column() < b.getPosition().column()) 
	    return lexicalfirst;
	if(a.getPosition().column() > b.getPosition().column()) 
	    return lexicalsecond;

	// Trying to compare the same advice, I guess... (modulo inlining behaviour)
	return GlobalAspectInfo.PRECEDENCE_NONE;

    }
    
    public List/*<MethodSig>*/ getProceeds() {
    	return proceeds;
    }
    
    public List/*<SootMethod>*/ getSootProceeds() {
    	List ret = new ArrayList();
    	for (Iterator procs = proceeds.iterator(); procs.hasNext(); ) {
    		MethodSig ms = (MethodSig) procs.next();
    		ret.add(ms.getSootMethod());
    	}
    	return ret;
    }
    	
}
