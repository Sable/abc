package abc.weaving.aspectinfo;

import java.util.*;
import soot.*;
import soot.jimple.*;
import soot.util.*;
import polyglot.util.Position;
import abc.weaving.matching.*;
import abc.weaving.weaver.WeavingContext;
import abc.weaving.weaver.PointcutCodeGen;
import abc.weaving.weaver.CodeGenException;
import abc.soot.util.LocalGeneratorEx;

/** An advice declaration. */
public class AdviceDecl extends AbstractAdviceDecl {

    private MethodSig impl;
    private Aspect aspect;
    private int jp,jpsp,ejp;

    private int nformals; // the number of formals in the advice implementation


    private Map/*<String,Integer>*/ formal_pos_map = new HashMap();
    private Map/*<String,AbcType>*/ formal_type_map = new HashMap();

    public AdviceDecl(AdviceSpec spec, Pointcut pc, MethodSig impl, Aspect aspect, 
		      int jp, int jpsp, int ejp, Position pos) {

	// the list of formals we give the super constructor for normalizing
	// is a little too large because it might include thisJoinPoint etc, 
	// but since people shouldn't be using that in pointcuts anyway this 
	// doesn't matter.

	super(spec,pc,impl.getFormals(),pos);
	this.impl = impl;
	this.aspect = aspect;
	this.jp = jp;
	this.jpsp = jpsp;
	this.ejp = ejp;

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

    /** Get the aspect containing this intertype method declaration.
     */
    public Aspect getAspect() {
	return aspect;
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
    public int numFormals() {
         return nformals;
    }

    public WeavingContext makeWeavingContext() {
	int nformals = numFormals();
	PointcutCodeGen.debug("There are " + nformals + " formals to the advice method.");
	Vector arglist = new Vector(nformals, 2);
	arglist.setSize(nformals);
	return new WeavingContext(arglist);
    }


    /** create the invoke to call the advice body */
    public Chain makeAdviceExecutionStmts
	(AdviceApplication adviceappl,
	 LocalGeneratorEx localgen,WeavingContext wc) {

	Chain c = new HashChain();
	SootClass theAspect=aspect.getInstanceClass().getSootClass();
	Local aspectref = localgen.generateLocal(theAspect.getType(),"theAspect");

	AssignStmt stmtAspectOf = Jimple.v().newAssignStmt
	    (aspectref, Jimple.v().newStaticInvokeExpr
	     (theAspect.getMethod("aspectOf", new ArrayList())));

	c.addLast(stmtAspectOf);

	SootMethod advicemethod = getImpl().getSootMethod();

	// try to fill in the remaining formals
	//   --- first the join point ones
	if (hasJoinPointStaticPart()) {
	    int position = joinPointStaticPartPos();
	    PointcutCodeGen.debug("The index for hasJoinPointStaticPart is " + position);

	    // FIXME: should really be ref to static field for SJP
	    StaticFieldRef sjpfieldref =
		Jimple.v().newStaticFieldRef(adviceappl.sjpInfo.sjpfield);
	    Local sjploc = localgen.generateLocal
		(RefType.v("org.aspectj.lang.JoinPoint$StaticPart"),"sjp");
	    
	    Stmt assignsjp = Jimple.v().newAssignStmt(sjploc, sjpfieldref);
	    c.addLast(assignsjp);
	    PointcutCodeGen.debug
		("inserting at postion "
		 + position
		 + " into a Vector of size "
		 + wc.arglist.capacity());

	    wc.arglist.setElementAt(sjploc, position);
	}

	if (hasJoinPoint()) {
	    PointcutCodeGen.debug("The index for hasJoinPoint is " + joinPointPos());
	}

	if (hasEnclosingJoinPoint()) {
	    int position = enclosingJoinPointPos();
	    PointcutCodeGen.debug
		("The index for enclosingJoinPoint is "
		 + enclosingJoinPointPos());
	    StaticFieldRef sjpencfieldref =
		Jimple.v().newStaticFieldRef(adviceappl.sjpEnclosing.sjpfield);
	    PointcutCodeGen.debug("The field ref is " + sjpencfieldref);
	    Local sjpencloc = localgen.generateLocal
		(RefType.v("org.aspectj.lang.JoinPoint$StaticPart"),"sjpenc");
	    Stmt assignsjpenc = Jimple.v().newAssignStmt(sjpencloc, sjpencfieldref);
	    c.addLast(assignsjpenc);
	    wc.arglist.setElementAt(sjpencloc, position);
	}

	boolean alldone = true;
	for (int i = 0; i < wc.arglist.size(); i++)
	    alldone = alldone && wc.arglist.get(i) != null;

	if (alldone) {
	    Stmt s =Jimple.v().newInvokeStmt
		(Jimple.v().newVirtualInvokeExpr
		 (aspectref,advicemethod,wc.arglist)
		 );
	    c.addLast(s);
	    return (c);
	} else
	    throw new CodeGenException
		("case not handled yet in making invoke to "
		 + advicemethod.getName());
    }


    public String toString() {
	return "(in aspect "+aspect.getInstanceClass().getName()+") "+spec+": "+pc+" >> "+impl+" <<"
	    +(hasJoinPoint() ? " thisJoinPoint" : "")
	    +(hasJoinPointStaticPart() ? " thisJoinPointStaticPart" : "")
	    +(hasEnclosingJoinPoint() ? " thisEnclosingJoinPoint" : "");
    }

    public void debugInfo(String prefix,StringBuffer sb) {
	sb.append(prefix+" in aspect: "+aspect.getInstanceClass().getName()+"\n");
	sb.append(prefix+" type: "+spec+"\n");
	sb.append(prefix+" pointcut: "+pc+"\n");
	sb.append(prefix+" implementation: "+impl+"\n");
    }

    public WeavingEnv getWeavingEnv() {
	// FIXME: cache this?
	return new AdviceFormals(this);
    }
}
