package abc.weaving.aspectinfo;

import java.util.Hashtable;

import polyglot.util.Position;

import soot.*;
import soot.jimple.*;

import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** Pointcut disjunction. */
public class OrPointcut extends Pointcut {
    private Pointcut pc1;
    private Pointcut pc2;

    public OrPointcut(Pointcut pc1, Pointcut pc2, Position pos) {
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
	return OrResidue.construct(pc1.matchesAt(we,cls,method,sm),
				   pc2.matchesAt(we,cls,method,sm));
    }

    protected Pointcut inline(Hashtable renameEnv,Hashtable typeEnv,Aspect context) {
	Pointcut pc1=this.pc1.inline(renameEnv,typeEnv,context);
	Pointcut pc2=this.pc2.inline(renameEnv,typeEnv,context);
	if(pc1==this.pc1 && pc2==this.pc2) return this;
	else return new OrPointcut(pc1,pc2,getPosition());
    }

    public String toString() {
	return "("+pc1+") || ("+pc2+")";
    }

    public void registerSetupAdvice() {
	pc1.registerSetupAdvice();
	pc2.registerSetupAdvice();
    }
}
