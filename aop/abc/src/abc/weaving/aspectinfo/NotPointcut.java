package abc.weaving.aspectinfo;

import java.util.*;

import polyglot.util.Position;

import soot.*;
import soot.jimple.*;

import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** Pointcut negation. */
public class NotPointcut extends Pointcut {
    private Pointcut pc;

    public NotPointcut(Pointcut pc, Position pos) {
	super(pos);
	this.pc = pc;
    }

    public Pointcut getPointcut() {
	return pc;
    }

    public Residue matchesAt(WeavingEnv we,
			     SootClass cls,
			     SootMethod method,
			     ShadowMatch sm) {
	return NotResidue.construct(pc.matchesAt(we,cls,method,sm));
    }

    protected Pointcut inline(Hashtable renameEnv,
			      Hashtable typeEnv,
			      Aspect context) {
	Pointcut pc=this.pc.inline(renameEnv,typeEnv,context);
	if(pc==this.pc) return this;
	else return new NotPointcut(pc,getPosition());
    }

    public String toString() {
	return "!("+pc+")";
    }

    public void registerSetupAdvice(Aspect context,Hashtable typeMap) {
	pc.registerSetupAdvice(context,typeMap);
    }


    public void getFreeVars(Set result) {
	pc.getFreeVars(result);
    }
}
