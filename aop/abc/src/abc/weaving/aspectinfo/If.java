package abc.weaving.aspectinfo;

import java.util.*;
import polyglot.util.Position;
import soot.*;
import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** Handler for <code>if</code> condition pointcut. */
public class If extends Pointcut {
    private List/*<Var>*/ vars;
    private MethodSig impl;

    public If(List vars, MethodSig impl, Position pos) {
	super(pos);
	this.vars = vars;
	this.impl = impl;
    }

    /** Get the pointcut variables that should be given as arguments to
     *  the method implementing the <code>if</code> condition.
     *  @return a list of {@link abc.weaving.aspectinfo.Var} objects.
     */
    public List getVars() {
	return vars;
    }

    /** Get the signature of the method implementing
     *  the <code>if</code> condition.
     */
    public MethodSig getImpl() {
	return impl;
    }

    public String toString() {
	return "if(...)";
    }

    public Residue matchesAt(WeavingEnv we,SootClass cls,SootMethod method,ShadowMatch sm) {
	List/*<WeavingVar>*/ args=new LinkedList();
	Iterator it=vars.iterator();
	while(it.hasNext()) args.add(we.getWeavingVar((Var) it.next()));
	return new IfResidue(impl.getSootMethod(),args);
    }

    protected Pointcut inline(Hashtable renameEnv,
			      Hashtable typeEnv,
			      Aspect context) {
	Iterator it=vars.iterator();
	List newvars=new LinkedList();
	while(it.hasNext())
	    newvars.add(((Var) it.next()).rename(renameEnv));
	return new If(newvars,impl,getPosition());
    }

    public void registerSetupAdvice(Aspect context,Hashtable typeMap) {}
    public void getFreeVars(Set/*<String>*/ result) {
	// just want binding occurrences, so do nothing
    }

}
