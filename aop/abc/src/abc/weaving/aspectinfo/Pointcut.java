package abc.weaving.aspectinfo;

import java.util.*;

import soot.*;
import soot.jimple.*;

import abc.weaving.matching.*;
import abc.weaving.residues.Residue;

import polyglot.util.Position;

/** A pointcut designator.
 *  @author Ganesh Sittampalam
 *  @date 28-Apr-04
 */
public abstract class Pointcut extends Syntax {

    public Pointcut(Position pos) {
	super(pos);
    }

    /** Force subclasses to define toString */
    public abstract String toString();

    /** Given a context and weaving environment,
     *  produce a residue
     */
    public abstract Residue matchesAt
	(WeavingEnv env,SootClass cls,
	 SootMethod method,ShadowMatch sm);

    /** Return a "normalized" version of this
     *  pointcut; with the following properties:
     *  All named pointcuts inlined
     */
    // Ought to lift local variables to one block at
    // the top and cache the weaving env or something
    public static Pointcut normalize(Pointcut pc,
				     List/*<Formal>*/ formals,
				     Aspect context) {

	Hashtable/*<String,Var>*/ renameEnv=new Hashtable();
	Hashtable/*<String,AbcType>*/ typeEnv=new Hashtable();

	if(formals!=null) {
	    Iterator it=formals.iterator();
	    while(it.hasNext()) {
		Formal f=(Formal) it.next();
		typeEnv.put(f.getName(),f.getType());
	    }
	}

	Pointcut ret=pc.inline(renameEnv,typeEnv,context);
	if(abc.main.Debug.v().showNormalizedPointcuts)
	    System.err.println("normalized pointcut: "+ret);

	ret.registerSetupAdvice(context,typeEnv);
	return ret;
    }

    protected abstract Pointcut inline
	(Hashtable/*<String,Var>*/ renameEnv,
	 Hashtable/*<String,AbcType>*/ typeEnv,
	 Aspect context);

    private static int freshVarNum=0;
    /** Return a freshly named pointcut variable */
    public static String freshVar() {
	return "pcvar$"+(freshVarNum++);
    }
    // changed to protected since other people shouldn't need to call it, 
    // but I can't be bothered to change the modifiers on the subclasses
    protected abstract void registerSetupAdvice
	(Aspect aspect,Hashtable/*<String,AbcType>*/ typeMap);
    // Get a list of free variables bound by this pointcut
    public abstract void getFreeVars(Set/*<String>*/ result);

}
