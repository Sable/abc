package abc.weaving.aspectinfo;

import java.util.*;
import polyglot.util.Position;
import soot.*;
import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** Handler for <code>cflow</code> condition pointcut. */
public class Cflow extends Pointcut {
    private Pointcut pc;

    public Cflow(Pointcut pc,Position pos) {
	super(pos);
	this.pc = pc;
    }

    public Pointcut getPointcut() {
	return pc;
    }

    public String toString() {
	return "cflow("+pc+")";
    }

    protected Pointcut inline(Hashtable renameEnv,
			      Hashtable typeEnv,
			      Aspect context) {
	Pointcut pc=this.pc.inline(renameEnv,typeEnv,context);
	if(pc==this.pc) return this;
	else return new Cflow(pc,getPosition());
    }

    private CflowSetup setupAdvice;

    public void registerSetupAdvice(Aspect context,Hashtable typeMap) {
	setupAdvice=CflowSetup.construct(context,pc,false,typeMap,getPosition());
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
