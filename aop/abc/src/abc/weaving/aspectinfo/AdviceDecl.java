
package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

import java.util.*;

/** An advice declaration. */
public class AdviceDecl extends Syntax {
    private AdviceSpec spec;
    private Pointcut pc;
    private MethodSig impl;
    private Aspect aspect;
    private int jp,jpsp,ejp;
    private Position pos;

    private int nformals; // the number of formals in the advice implementation
    private int applcount=0; // the number of times this AdviceDecl matches
                             //   (i.e. the number of static join points)

    private Map/*<String,Integer>*/ formal_pos_map = new HashMap();
    private Map/*<String,AbcType>*/ formal_type_map = new HashMap();

    public AdviceDecl(AdviceSpec spec, Pointcut pc, MethodSig impl, Aspect aspect, int jp, int jpsp, int ejp, Position pos) {
	super(pos);
	this.spec = spec;
	this.pc = pc;
	this.impl = impl;
	this.aspect = aspect;
	this.jp = jp;
	this.jpsp = jpsp;
	this.ejp = ejp;
	this.pos = pos;

	if (spec instanceof AbstractAdviceSpec) {
	    ((AbstractAdviceSpec)spec).setAdvice(this);
	}

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
	

    public AdviceSpec getAdviceSpec() {
	return spec;
    }

    public Pointcut getPointcut() {
	return pc;
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

    /** Increment the number of times this advice is applied, and return
     *  incremented value.
     */
    public int incrApplCount() {
        applcount++;
	return(applcount);
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
}
