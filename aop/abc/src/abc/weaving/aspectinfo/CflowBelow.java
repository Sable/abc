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
	private Hashtable/*<String,Var>*/ renaming;

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

	GlobalCflowSetupFactory.CflowSetupContainer cfsCont = 
	    GlobalCflowSetupFactory.construct(context,pc,true,typeMap,getPosition(),depth);

	setupAdvice = cfsCont.getCfs();
	renaming = cfsCont.getRenaming();

	// Should only do this if the advice has not already been added.

	if (cfsCont.isFresh()) {
	    GlobalAspectInfo.v().addAdviceDecl(setupAdvice);
	}

    }

    public Residue matchesAt
	(WeavingEnv env,SootClass cls,
	 SootMethod method,ShadowMatch sm) {

	List/*<Var>*/ actuals=setupAdvice.getActuals();
	// List of actuals for the Cflow setup advice
	// These are NOT necessarily the same as the actuals for
	// this (inlined) pointcut, but we have the renaming
	List/*<WeavingVar>*/ weavingActuals=new LinkedList();
	Iterator it=actuals.iterator();
	while(it.hasNext()) {
		Var setupvar = (Var) it.next();
		Var inlinedvar = (Var) renaming.get(setupvar.getName());
		if (inlinedvar == null) {
			throw new RuntimeException("Internal error: Could not find variable "+
					setupvar.getName() + " in cflow renaming");
		}
		weavingActuals.add(env.getWeavingVar(inlinedvar));
	}
	return new CflowResidue(setupAdvice,weavingActuals);
    }

    public void getFreeVars(Set result) {
	pc.getFreeVars(result);
    }

    public boolean equivalent(Pointcut otherpc) {
	if (otherpc instanceof CflowBelow) {
	    return pc.equivalent(((CflowBelow)otherpc).getPointcut());
	} else return false;
    }

	/* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.Pointcut#equivalent(abc.weaving.aspectinfo.Pointcut, java.util.Hashtable)
	 */
	public boolean equivalent(Pointcut otherpc, Hashtable renaming) {
		if (otherpc instanceof CflowBelow) {
			return pc.equivalent(((CflowBelow)otherpc).getPointcut(), renaming);
		} else return false;
	}

}
