package abc.weaving.aspectinfo;

import java.util.*;

import polyglot.util.Position;
import polyglot.types.SemanticException;

import soot.*;
import soot.jimple.*;

import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** Pointcut negation. */
public class NotPointcut extends Pointcut {
    private Pointcut pc;

    private NotPointcut(Pointcut pc, Position pos) {
	super(pos);
	this.pc = pc;
    }

    public static Pointcut construct(Pointcut pc, Position pos) {
	if(pc instanceof EmptyPointcut) return new FullPointcut(pos);
	if(pc instanceof FullPointcut) return new EmptyPointcut(pos);
	return new NotPointcut(pc,pos);
    }
    
    public Pointcut getPointcut() {
	return pc;
    }

    public Residue matchesAt(WeavingEnv we,
			     SootClass cls,
			     SootMethod method,
			     ShadowMatch sm)
	throws SemanticException
    {
	return NotResidue.construct(pc.matchesAt(we,cls,method,sm));
    }

    protected Pointcut inline(Hashtable renameEnv,
			      Hashtable typeEnv,
			      Aspect context) {
	Pointcut pc=this.pc.inline(renameEnv,typeEnv,context);
	if(pc==this.pc) return this;
	else return construct(pc,getPosition());
    }

    protected DNF dnf() {
	return new DNF(new NotPointcut
		       (pc.dnf().makePointcut(pc.getPosition()),getPosition()));
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

    public boolean equivalent(Pointcut otherpc) {
	if (otherpc instanceof NotPointcut) {
	    return pc.equivalent(((NotPointcut)otherpc).getPointcut());
	} else return false;
    }

}
