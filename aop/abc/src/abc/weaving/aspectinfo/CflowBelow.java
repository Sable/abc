package abc.weaving.aspectinfo;

import java.util.Hashtable;
import polyglot.util.Position;
import soot.*;
import abc.weaving.matching.*;
import abc.weaving.residues.Residue;

/** Handler for <code>cflowbelow</code> condition pointcut. */
public class CflowBelow extends Pointcut {
    private Pointcut pc;

    public CflowBelow(Pointcut pc,Position pos) {
	super(pos);
	this.pc = pc;
    }

    public Pointcut getPointcut() {
	return pc;
    }

    public String toString() {
	return "cflowbelow("+pc+")";
    }

    protected Pointcut inline(Hashtable renameEnv,
			      Hashtable typeEnv,
			      Aspect aspect) {
	Pointcut pc=this.pc.inline(renameEnv,typeEnv,aspect);
	if(pc==this.pc) return this;
	else return new CflowBelow(pc,getPosition());
    }

    public void registerSetupAdvice() {
	// FIXME
    }

    public Residue matchesAt
	(WeavingEnv env,SootClass cls,
	 SootMethod method,ShadowMatch sm) 
    { return null; }

}
