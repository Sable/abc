package abc.weaving.aspectinfo;

import java.util.*;
import polyglot.util.Position;
import soot.*;
import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** Handler for <code>cflowbelow</code> condition pointcut. */
public class CflowBelow extends Pointcut {
    private Pointcut pc;
    int depth;

    public CflowBelow(Pointcut pc,Position pos,int depth) {
	super(pos);
	this.pc = pc;
	this.depth=depth;
    }

    public Pointcut getPointcut() {
	return pc;
    }

    public int getDepth() {
	return depth;
    }

    protected DNF dnf() {
	return new DNF
	    (new CflowBelow
	     (pc.dnf().makePointcut(pc.getPosition()),getPosition(),depth));
    }

    public String toString() {
	return "cflowbelow("+pc+")";
    }

    protected Pointcut inline(Hashtable renameEnv,
			      Hashtable typeEnv,
			      Aspect context) {
	Pointcut pc=this.pc.inline(renameEnv,typeEnv,context);
	if(pc==this.pc) return this;
	else return new CflowBelow(pc,getPosition(),depth);
    }

    private CflowSetup setupAdvice;

    public void registerSetupAdvice(Aspect context,Hashtable typeMap) {
	setupAdvice=CflowSetup.construct(context,pc,true,typeMap,getPosition(),depth);
	GlobalAspectInfo.v().addAdviceDecl(setupAdvice);
    }

    public Residue matchesAt
	(WeavingEnv env,SootClass cls,
	 SootMethod method,ShadowMatch sm) {

	List/*<Var>*/ actuals=setupAdvice.getActuals();
	List/*<WeavingVar>*/ weavingActuals=new LinkedList();
	Iterator it=actuals.iterator();
	while(it.hasNext()) 
	    weavingActuals.add(env.getWeavingVar((Var) it.next()));
	return new CflowResidue(setupAdvice,weavingActuals);
    }

    public void getFreeVars(Set result) {
	pc.getFreeVars(result);
    }
}
