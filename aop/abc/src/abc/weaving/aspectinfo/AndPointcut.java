package abc.weaving.aspectinfo;

import java.util.*;

import polyglot.util.Position;
import polyglot.types.SemanticException;

import soot.*;
import soot.jimple.*;

import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** Pointcut conjunction. */
public class AndPointcut extends Pointcut {
    private Pointcut pc1;
    private Pointcut pc2;

    private AndPointcut(Pointcut pc1, Pointcut pc2, Position pos) {
	super(pos);
	this.pc1 = pc1;
	this.pc2 = pc2;
    }

    public static Pointcut construct(Pointcut pc1, Pointcut pc2, Position pos) {
	if(pc1 instanceof EmptyPointcut || pc2 instanceof FullPointcut) return pc1;
	if(pc2 instanceof EmptyPointcut || pc1 instanceof FullPointcut) return pc2;
	return new AndPointcut(pc1,pc2,pos);
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
			     ShadowMatch sm)
	throws SemanticException
    {
	return AndResidue.construct(pc1.matchesAt(we,cls,method,sm),
				    pc2.matchesAt(we,cls,method,sm));
    }
    
    protected Pointcut inline(Hashtable renameEnv,Hashtable typeEnv, Aspect context) {
	Pointcut pc1=this.pc1.inline(renameEnv,typeEnv,context);
	Pointcut pc2=this.pc2.inline(renameEnv,typeEnv,context);
	if(pc1==this.pc1 && pc2==this.pc2) return this;
	else return construct(pc1,pc2,getPosition());
    }

    protected DNF dnf() {
	DNF dnf1=pc1.dnf();
	DNF dnf2=pc2.dnf();
	return DNF.and(dnf1,dnf2);
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

    public boolean equivalent(Pointcut otherpc) {
	if (otherpc instanceof AndPointcut) {
	    return (   (pc1.equivalent(((AndPointcut)otherpc).getLeftPointcut()))
		    && (pc2.equivalent(((AndPointcut)otherpc).getRightPointcut())));
	} else return false;
    }

	/* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.Pointcut#equivalent(abc.weaving.aspectinfo.Pointcut, java.util.Hashtable)
	 */
	public boolean equivalent(Pointcut otherpc, Hashtable renaming) {
		/* if matches, subst is the union of the substs for left and right */
		
		if (otherpc instanceof AndPointcut) {
			AndPointcut otherand = (AndPointcut)otherpc;
			if (pc1.equivalent(otherand.getLeftPointcut(), renaming)) {
				return (pc2.equivalent(otherand.getRightPointcut(), renaming));
			} else return false;
		} else return false;
	}

}
