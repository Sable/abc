package abc.weaving.aspectinfo;

import java.util.*;
import polyglot.util.Position;

import soot.*;
import soot.jimple.*;

import abc.weaving.matching.*;
import abc.weaving.residues.Residue;

/** A pointcut designator representing a condition on the 
 *  lexical context
 *  @author Ganesh Sittampalam
 *  @date 30-Apr-04
 */
public abstract class LexicalPointcut extends Pointcut {
    public LexicalPointcut(Position pos) {
	super(pos);
    }

    public final Residue matchesAt(WeavingEnv env,
				   SootClass cls,
				   SootMethod method,
				   ShadowMatch sm) {
	return matchesAt(cls,method);
    }

    /** Do we match at a particular class and method? */
    protected abstract Residue matchesAt(SootClass cls,
					 SootMethod method);

    protected Pointcut inline(Hashtable typeEnv,
			      Hashtable renameEnv,
			      Aspect context) {
	return this;
    }

    public void registerSetupAdvice
	(Aspect aspect,Hashtable/*<String,AbcType>*/ typeMap) {}

    public void getFreeVars(Set/*<String>*/ result) {}


}
