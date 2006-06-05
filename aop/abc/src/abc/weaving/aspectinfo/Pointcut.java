/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2006 Eric Bodden
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import polyglot.types.SemanticException;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import abc.weaving.matching.MatchingContext;
import abc.weaving.residues.Residue;

/** This is the base class for pointcut designators; it is constructed by the frontend.
 *  A pointcut designator is primarily responsible for calculating the residue for itself
 *  at a given join point shadow.
 *
 *  @author Ganesh Sittampalam
 *  @author Eric Bodden
 */
public abstract class Pointcut extends Syntax {

    /** @param The source position of the pointcut */
    public Pointcut(Position pos) {
        super(pos);
    }

    /** Subclasses must define toString, for debugging purposes */
    public abstract String toString();

    /** Given a context and weaving environment,
     *  produce a residue
     */
    public abstract Residue matchesAt
        (MatchingContext mc)
        throws SemanticException;

    /** Return a "normalized" version of this pointcut.
     *  The result has the following properties:
     *  <ul>
     *  <li>All named pointcuts inlined
     *  <li>All locally quantified variables have fresh names
     *  <li>Converted to DNF
     *  <li>All <code>cflow</code>s/<code>cflowbelow</code>s have 
     *  been "registered" as separate pointcuts
     *  </ul>
     *  @param pc The pointcut to normalize
     *  @param formals A list of {@link Formal}s that are in scope for this
     *                 pointcut
     *  @param context The aspect in which the pointcut occurs
     *  @return The normalized pointcut
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

        Pointcut inlined=pc.inline(renameEnv,typeEnv,context,0);

        Pointcut ret=inlined.dnf().makePointcut(pc.getPosition());

        if(abc.main.Debug.v().showNormalizedPointcuts)
            System.err.println("normalized pointcut: "+ret);

        ret.registerSetupAdvice(context,typeEnv);
        return ret;
    }

    /** This class is used to calculate the disjunctive normal form
     *  of pointcuts. This needs to happen to efficiently implement 
     *  the backtracking semantics of disjunction. This structure is
     *  built compositionally for compound pointcuts, and then converted 
     *  back to a {@link Pointcut} when the final version is required.
     *  This structure should only be constructed for pointcuts that
     *  do not have any name clashes (the pointcuts returned by the inline
     *  method have this property).
     */
    public final static class DNF {
        private List/*<Formal>*/ formals;
        private List/*<List<Pointcut>>*/ disjuncts;

	/** Construct DNF from a singleton pointcut */
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

	/** Add a new formal that is in scope somewhere. The precise
	 *  scope is lost, so this is only safe for pointcuts that are
         *  valid and contain no name clashes.
         */
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

        /** Turn the DNF back into a pointcut
         *  @param pos The source position of the original pointcut
	 *  @return The resulting pointcut
         */
        public Pointcut makePointcut(Position pos) {
            Pointcut pc=makeDisjuncts(disjuncts,pos);
            if(!formals.isEmpty())
                pc=new LocalPointcutVars(pc,formals,pos);
            return pc;
        }
    }

    /** Return the DNF form of this pointcut. For pointcuts without
     *  children this default implementation is fine; for pointcuts with
     *  children it should be overridden so that at the very least 
     *  the children are converted to DNF.
     */
    public DNF dnf() {
        return new DNF(this);
    }

    /** Inlining should remove all PointcutRefs, and return a pointcut 
     *  that is alpha-renamed
     *  @param renameEnv  A mapping from pointcut names to the {@link Var}s 
     *                    they should be renamed to. If a name isn't in the 
     *                    map, it doesn't need to be renamed.
     *  @param typeEnv    A mapping from pointcut names to {@link AbcType}s. 
     *                    Every variable that can appear free in the pointcut 
     *                    must be listed. The names are those before any
     *                    renaming takes place.
     *  @param context    The {@link Aspect} in which the root pointcut is
     *                    defined. This is required because references to
     *                    abstract pointcuts must be resolved to the concrete
     *                    pointcut using this aspect.
     *  @param cflowdepth The number of surrounding <code>cflow</code>s. 
     *                    This is required
     *                    to determine the correct precedence for the 
     *                    synthetic advice used to implement 
     *                    <code>cflow</code>.
     *  @return The inlined pointcut
     */
    // It would be better if the list of parameters was wrapped up into
    // a class, so we don't need to change everything each time we add one
    public abstract Pointcut inline
        (Hashtable/*<String,Var>*/ renameEnv,
         Hashtable/*<String,AbcType>*/ typeEnv,
         Aspect context,
	 int cflowdepth);

    private static int freshVarNum=0;
    /** Return a freshly named pointcut variable */
    public static String freshVar() {
        return "pcvar$"+(freshVarNum++);
    }

    // changed to protected since other people shouldn't need to call it,
    // but I can't be bothered to change the modifiers on the subclasses
    /** If any synthetic advice is required to implement this pointcut,
     *  this method should take care of adding it.
     *  @param context The aspect in which the pointcut is defined
     *  @param typeEnv A mapping from formal name to {@link AbcType} for
     *                 all the formal parameters to the pointcut
     */
    public abstract void registerSetupAdvice
        (Aspect context,Hashtable/*<String,AbcType>*/ typeEnv);

    /** Get a list of free variable names bound by this pointcut.
     *   @param result The results should be placed in this set
     *                 (having it as a parameter allows it to be built up
     *                 incrementally, which is more efficient than 
     *                 repeatedly taking the union of sets)
     */
    public abstract void getFreeVars(Set/*<String>*/ result);

    /** Attempt to unify two pointcuts. pc.unify(pc', unification)
     *  should return true if the pointcuts can be unified, and 
     *  set the renamings appropriately in unification. There are
     *  two cases for unification: if unification.unifyWithFirst()
     *  is true, then the unification should only succeed if this
     *  pc can be renamed to pc', with the unification pointcut equal
     *  to this. Otherwise, the unification pointcut can be anything,
     *  as long as it can be renamed both to this and pc'.
     *  <p>
     *  A default implementation is provided, but all subclasses should
     *  override this - otherwise cflow CSE will be disabled for cflow
     *  that use these pointcuts.
     *  <p>
     *  Typical implementations for pointcuts that introduce no free
     *  variables are straightforward (see the And pointcut, for example).
     *  For pointcuts that introduce free variables, the Var.unify method
     *  is used to actually update the renamings (see the Args pointcut).
     *  @param otherpc the pointcut that should be unify with this
     *  @param unification the Unification that should be set. 
     *  @return true iff the unification is succesful. If true is returned,
     *  then Unification.setPointcut(the unified pointcut) must have been
     *  called in the body of unify().
     *  @see abc.weaving.aspectinfo.AndPointcut#unify(Pointcut, Unification) AndPointcut.unify example
     *  @see abc.weaving.aspectinfo.Var#unify(Var, Unification) Var.unify
     *  @see abc.weaving.aspectinfo.Unification
     */
    public boolean unify(Pointcut otherpc,Unification unification) {

        if (otherpc != this) return false;

        // pc.unify(pc, unification) succeeds, setting the result of the
        // unification to pc with the identity map on the free vars of pc

        Set fvs = new HashSet();
        getFreeVars(fvs);
        Iterator it = fvs.iterator();

        while (it.hasNext()) {
            String s = (String) it.next();
            Var v = new Var(s, getPosition());
            unification.putVar1(v, v);
            unification.putVar2(v, v);
        }
        unification.setPointcut(this);
        return true;

    }

}
