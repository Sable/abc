package abc.weaving.aspectinfo;

import java.util.Hashtable;
import polyglot.util.Position;
import soot.*;
import abc.weaving.matching.*;
import abc.weaving.residues.Residue;

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
			      Aspect aspct) {
	Pointcut pc=this.pc.inline(renameEnv,typeEnv,aspct);
	if(pc==this.pc) return this;
	else return new Cflow(pc,getPosition());
    }

    public void registerSetupAdvice() {
	// FIXME
    }

    public Residue matchesAt
	(WeavingEnv env,SootClass cls,
	 SootMethod method,ShadowMatch sm) 
    { return null; }
	
}
