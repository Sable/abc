package abc.weaving.aspectinfo;

import java.util.*;
import polyglot.util.Position;
import soot.*;
import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** Handler for <code>cflow</code> condition pointcut. */
public class Cflow extends Pointcut {
    private Pointcut pc;
    private int depth;

    public Cflow(Pointcut pc,Position pos,int depth) {
	super(pos);
	this.pc = pc;
	this.depth = depth;
    }

    public Pointcut getPointcut() {
	return pc;
    }

    public int getDepth() {
	return depth;
    }

    public String toString() {
	return "cflow("+pc+")";
    }

    protected Pointcut inline(Hashtable renameEnv,
			      Hashtable typeEnv,
			      Aspect context) {
	Pointcut pc=this.pc.inline(renameEnv,typeEnv,context);
	if(pc==this.pc) return this;
	else return new Cflow(pc,getPosition(),depth);
    }

    protected DNF dnf() {
	return new DNF
	    (new Cflow(pc.dnf().makePointcut(pc.getPosition()),getPosition(),depth));
    }

    private CflowSetup setupAdvice;

    public void registerSetupAdvice(Aspect context,Hashtable typeMap) {

	GlobalCflowSetupFactory.CflowSetupContainer cfsCont = 
	    GlobalCflowSetupFactory.construct(context,pc,false,typeMap,getPosition(),depth);

	setupAdvice = cfsCont.getCfs();

	// Should only do this if the advice has not already been added.

	if (cfsCont.isFresh()) {
	    GlobalAspectInfo.v().addAdviceDecl(setupAdvice);
	}
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

    public boolean equivalent(Pointcut otherpc) {
	if (otherpc instanceof Cflow) {
	    return pc.equivalent(((Cflow)otherpc).getPointcut());
	} else return false;
    }
	
}
