/* Abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL; 
 * if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package abc.weaving.aspectinfo;

import java.util.*;

import soot.*;
import soot.jimple.*;

import abc.weaving.matching.*;
import abc.weaving.residues.Residue;

import polyglot.util.Position;
import polyglot.util.InternalCompilerError;
import polyglot.types.SemanticException;

/** This is the base class for pointcut designators; it is constructed by the frontend
 *  A pointcut designator is primarily responsible for calculating the residue for itself
 *  at a given join point shadow.
 * 
 *  @author Ganesh Sittampalam
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
    public static Pointcut normalize(Pointcut pc,
				     List/*<Formal>*/ formals,
				     Aspect context) {

	Hashtable/*<String,Var>*/ renameEnv=new Hashtable();
	Hashtable/*<String,AbcType>*/ typeEnv=new Hashtable();

	if(formals!=null) {
	    Iterator it=formals.iterator();
	    while(it.hasNext()) {
		Formal f=(Formal) it.next();
		if(f.getName()==null)
		    throw new InternalCompilerError("formal with null name: "+f);
		if(f.getType()==null)
		    throw new InternalCompilerError("formal with null type: "+f);
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
		// a "not" might have a nested if, and since it can't bind anything 
		// moving it can't matter
		if(cur instanceof If || cur instanceof NotPointcut) 
		    ifs=AndPointcut.construct(ifs,cur,pos);
		else res=AndPointcut.construct(res,cur,pos);
	    }
	    return AndPointcut.construct(res,ifs,pos);
	}

	private static Pointcut makeDisjuncts(List/*<List<Pointcut>>*/ disjuncts,Position pos) {
	    Iterator it=disjuncts.iterator();
	    Pointcut res=new EmptyPointcut(pos);
	    while(it.hasNext()) {
		res=OrPointcut.construct(res,makeConjuncts((List) it.next(),pos),pos);
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

    /** Compare for equivalence to another pc, modulo alpha conversion and 
     *  abstraction of some free variables
     *  i.e. pc1.canRenameTo(pc2, renaming) should return true if: pc1 is
     *  structurally equivalent to pc2, except that the free variables of 
     *  pc1 can have different names; also, pc1 can have MORE free variables
     *  than pc2. Sets renaming to map each free var in pc1 that is also in
     *  pc2 to the corresponding var in pc2, and maps vars in pc1 that aren't
     *  free in pc2 to a dummy PointcutVarEntry object
     * 
     *  Any class that descends from Pointcut should implement/override this   
     * 
     * 
     * @param otherpc The pointcut to compare against
     * @param renaming A Hashtable<Var,PointcutVarEntry> to contain the renaming.
     * Should be empty but initialised when calling canRenameTo
     * @return
     */
	public abstract boolean canRenameTo(Pointcut otherpc, 
									   Hashtable/*<Var,PointcutVarEntry>*/ renaming);

}
