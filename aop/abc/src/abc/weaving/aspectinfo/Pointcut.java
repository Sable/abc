/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
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

import java.util.*;

import soot.*;
import soot.jimple.*;

import abc.weaving.matching.*;
import abc.weaving.residues.Residue;

import polyglot.util.Position;
import polyglot.util.InternalCompilerError;
import polyglot.types.SemanticException;

/** This is the base class for pointcut designators; it is constructed by the frontend.
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

        Pointcut inlined=pc.inline(renameEnv,typeEnv,context,0);

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
    // It would be better if the list of parameters was wrapped up into
    // a class, so we don't need to change everything each time we add one
    protected abstract Pointcut inline
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
    protected abstract void registerSetupAdvice
        (Aspect aspct,Hashtable/*<String,AbcType>*/ typeMap);

    // Get a list of free variables bound by this pointcut
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
