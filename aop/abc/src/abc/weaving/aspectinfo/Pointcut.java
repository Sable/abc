package abc.weaving.aspectinfo;

import java.util.*;

import soot.*;
import soot.jimple.*;

import abc.weaving.matching.*;
import abc.weaving.residues.Residue;

import polyglot.util.Position;
import polyglot.types.SemanticException;

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
	 SootMethod method,ShadowMatch sm)
	throws SemanticException;

    /** Return a "normalized" version of this
     *  pointcut; with the following properties:
     *  All named pointcuts inlined
     *  All locally quantified variables have fresh names
     *  Converted to DNF
     *  All cflows/cflowbelows have been "registered" as separate pointcuts
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

	Pointcut inlined=pc.inline(renameEnv,typeEnv,context);

	Pointcut ret=inlined.dnf().makePointcut(pc.getPosition());

	if(abc.main.Debug.v().showNormalizedPointcuts)
	    System.err.println("normalized pointcut: "+ret);

	ret.registerSetupAdvice(context,typeEnv);
	return ret;
    }

    protected final static class DNF {
	private List/*<Formal>*/ formals;
	private List/*<List<Pointcut>>*/ disjuncts;

	public DNF(Pointcut pc) {
	    formals=new ArrayList();
	    disjuncts=new ArrayList();
	    List conjuncts=new ArrayList(1);
	    conjuncts.add(pc);
	    disjuncts.add(conjuncts);
	}

	public static DNF or(DNF dnf1,DNF dnf2) {
	    dnf1.formals.addAll(dnf2.formals);
	    dnf1.disjuncts.addAll(dnf2.disjuncts);
	    return dnf1;
	}

	public static DNF declare(DNF dnf,List formals) {
	    dnf.formals.addAll(formals);
	    return dnf;
	}

	private DNF() {
	}

	public static DNF and(DNF dnf1,DNF dnf2) {
	    DNF res=new DNF();
	    res.formals=dnf1.formals;
	    res.formals.addAll(dnf2.formals);

	    res.disjuncts=new ArrayList(dnf1.disjuncts.size()*dnf2.disjuncts.size());

	    Iterator left=dnf1.disjuncts.iterator();
	    while(left.hasNext()) {
		final List/*<Pointcut>*/ leftConjuncts=(List) left.next();
		Iterator right=dnf2.disjuncts.iterator();
		while(right.hasNext()) {
		    final List/*<Pointcut>*/ rightConjuncts=(List) right.next();
                    
		    List conjuncts=new ArrayList(leftConjuncts.size()+rightConjuncts.size());
		    conjuncts.addAll(leftConjuncts);
		    conjuncts.addAll(rightConjuncts);
		    res.disjuncts.add(conjuncts);
		}
	    }
	    return res;
	}

	private static Pointcut makeConjuncts(List/*<Pointcut>*/ conjuncts,Position pos) {
	    Iterator it=conjuncts.iterator();
	    Pointcut res=new FullPointcut(pos);
	    Pointcut ifs=new FullPointcut(pos);
	    while(it.hasNext()) {
		Pointcut cur=(Pointcut) it.next();
		if(cur instanceof If)
		    ifs=new AndPointcut(ifs,cur,pos);
		else res=new AndPointcut(res,cur,pos);
	    }
	    return new AndPointcut(res,ifs,pos);
	}

	private static Pointcut makeDisjuncts(List/*<List<Pointcut>>*/ disjuncts,Position pos) {
	    Iterator it=disjuncts.iterator();
	    Pointcut res=new EmptyPointcut(pos);
	    while(it.hasNext()) {
		res=new OrPointcut(res,makeConjuncts((List) it.next(),pos),pos);
	    }
	    return res;
	}

	public Pointcut makePointcut(Position pos) {
	    Pointcut pc=makeDisjuncts(disjuncts,pos);
	    if(!formals.isEmpty())
		pc=new LocalPointcutVars(pc,formals,pos);
	    return pc;
	}
    }

    /** This method should be overridden in any derived class that has pointcut children */
    protected DNF dnf() {
	return new DNF(this);
    }

    /** Inlining should remove all PointcutRefs, 
     *  and return a pointcut that is alpha-renamed
     */
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
