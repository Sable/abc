package abc.weaving.aspectinfo;

import java.util.*;

import polyglot.util.Position;

import soot.*;
import soot.jimple.*;

import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** Pointcut conjunction. */
public class AndPointcut extends Pointcut {
    private Pointcut pc1;
    private Pointcut pc2;

    public AndPointcut(Pointcut pc1, Pointcut pc2, Position pos) {
	super(pos);
	this.pc1 = pc1;
	this.pc2 = pc2;
    }

    public Pointcut getLeftPointcut() {
	return pc1;
    }

    public Pointcut getRightPointcut() {
	return pc2;
    }

    public Residue matchesAt(WeavingEnv we,
			     SootClass cls,
			     SootMethod method,
			     ShadowMatch sm) {
	return AndResidue.construct(pc1.matchesAt(we,cls,method,sm),
				    pc2.matchesAt(we,cls,method,sm));
    }
    
    protected Pointcut inline(Hashtable renameEnv,Hashtable typeEnv, Aspect context) {
	Pointcut pc1=this.pc1.inline(renameEnv,typeEnv,context);
	Pointcut pc2=this.pc2.inline(renameEnv,typeEnv,context);
	if(pc1==this.pc1 && pc2==this.pc2) return this;
	else return new AndPointcut(pc1,pc2,getPosition());
    }

    public String toString() {
	return "("+pc1+") && ("+pc2+")";
    }

    public void registerSetupAdvice(Aspect context,Hashtable typeMap) {
	pc1.registerSetupAdvice(context,typeMap);
	pc2.registerSetupAdvice(context,typeMap);
    }

    public void getFreeVars(Set result) {
	pc1.getFreeVars(result);
	pc2.getFreeVars(result);
    }
}
