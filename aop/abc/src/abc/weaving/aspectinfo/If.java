/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2004 Damien Sereni
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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import polyglot.util.Position;
import soot.RefType;
import abc.weaving.matching.MatchingContext;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.AlwaysMatch;
import abc.weaving.residues.AndResidue;
import abc.weaving.residues.IfResidue;
import abc.weaving.residues.JoinPointInfo;
import abc.weaving.residues.Load;
import abc.weaving.residues.LocalVar;
import abc.weaving.residues.Residue;
import abc.weaving.residues.StaticJoinPointInfo;
import abc.weaving.residues.WeavingVar;

/** Handler for <code>if</code> condition pointcut.
 *  @author Aske Simon Christensen
 *  @author Ganesh Sittampalam
 *  @author Damien Sereni
 *  @author Eric Bodden
 */
public class If extends Pointcut {
    private List/*<Var>*/ vars;
    private MethodSig impl;

    int jp,jpsp,ejp;

    public If(List vars, MethodSig impl, int jp, int jpsp, int ejp, Position pos) {
        super(pos);
        this.vars = vars;
        this.impl = impl;

        this.jp = jp;
        this.jpsp = jpsp;
        this.ejp = ejp;
    }

    public boolean hasJoinPoint() {
        return jp != -1;
    }

    public boolean hasJoinPointStaticPart() {
        return jpsp != -1;
    }

    public boolean hasEnclosingJoinPoint() {
        return ejp != -1;
    }

    public int joinPointPos() {
        return jp;
    }

    public int joinPointStaticPartPos() {
        return jpsp;
    }

    public int enclosingJoinPointPos() {
        return ejp;
    }


    /** Get the pointcut variables that should be given as arguments to
     *  the method implementing the <code>if</code> condition.
     *  @return a list of {@link abc.weaving.aspectinfo.Var} objects.
     */
    public List getVars() {
        return vars;
    }

    /** Get the signature of the method implementing
     *  the <code>if</code> condition.
     */
    public MethodSig getImpl() {
        return impl;
    }

    public String toString() {
        return "if(...)";
    }

    protected Residue getWeavingVars(List vars, List args, MatchingContext mc)
    {
        WeavingEnv we = mc.getWeavingEnv();
        ShadowMatch sm = mc.getShadowMatch();

        Residue ret=AlwaysMatch.v();

        Iterator it=vars.iterator();
        int i=0;
        while(it.hasNext()) {
            WeavingVar wvar;
            Var var=(Var) it.next();

            if(i==joinPointStaticPartPos()) {
                wvar=new LocalVar(RefType.v("org.aspectj.lang.JoinPoint$StaticPart"),
                                 "thisJoinPointStaticPart");
                ret=AndResidue.construct
                    (ret,new Load(new StaticJoinPointInfo(sm.getSJPInfo()),wvar));
            } else if(i==enclosingJoinPointPos()) {
                wvar=new LocalVar(RefType.v("org.aspectj.lang.JoinPoint$StaticPart"),
                                 "thisEnclosingJoinPointStaticPart");
                ret=AndResidue.construct
                    (ret,new Load(new StaticJoinPointInfo(sm.getEnclosing().getSJPInfo()),wvar));
            } else if(i==joinPointPos()) {
                wvar=new LocalVar(abc.weaving.residues.JoinPointInfo.sootType(),
                                 "thisJoinPoint");
                ret=AndResidue.construct
                    (ret,new Load(new JoinPointInfo(sm),wvar));

                // make sure the SJP info will be around later for
                // the JoinPointInfo residue
                sm.recordSJPInfo();
            } else wvar=we.getWeavingVar(var);

            args.add(wvar);
            i++;
        }

        return ret;
    }

    public Residue matchesAt(MatchingContext mc) {
        List/*<WeavingVar>*/ args=new LinkedList();
        Residue ret = getWeavingVars(vars, args, mc);

        ret=AndResidue.construct(ret,IfResidue.construct(impl.getSootMethod(),args));
        return ret;
    }

    public Pointcut inline(Hashtable renameEnv,
                              Hashtable typeEnv,
                              Aspect context,
			      int cflowdepth) {
        Iterator it=vars.iterator();
        List newvars=new LinkedList();
        while(it.hasNext())
            newvars.add(((Var) it.next()).rename(renameEnv));
        return new If(newvars,impl,jp,jpsp,ejp,getPosition());
    }

    public void registerSetupAdvice(Aspect context,Hashtable typeMap) {}
    
    public void getFreeVars(Set/*<String>*/ result) {
        // just want binding occurrences, so do nothing
    }

        /* (non-Javadoc)
         * @see abc.weaving.aspectinfo.Pointcut#unify(abc.weaving.aspectinfo.Pointcut, java.util.Hashtable, java.util.Hashtable, abc.weaving.aspectinfo.Pointcut)
         */
        public boolean unify(Pointcut otherpc, Unification unification) {

                if (otherpc.getClass() == this.getClass()) {
                        If oif = (If)otherpc;

                        if (this.hasJoinPoint() != oif.hasJoinPoint()) return false;
                        if (this.hasJoinPointStaticPart() != oif.hasJoinPointStaticPart()) return false;
                        if (this.hasEnclosingJoinPoint() != oif.hasEnclosingJoinPoint()) return false;

                        // COMPARING VARS: POINTWISE UNIFICATION

                        Iterator it1 = vars.iterator();
                        Iterator it2 = oif.getVars().iterator();
                        List unifiedvars = new LinkedList();

                        // unificationType: should be set to
                        //   -1 if ALL vars come from THIS
                        //    1 if ALL vars come from OTHER
                        //    0 if vars come from both
                        //        2 if no vars yet
                        int unificationType = 2;

                        while (it1.hasNext() && it2.hasNext()) {
                                Var var1 = (Var) it1.next();
                                Var var2 = (Var) it2.next();

                                if (var1.unify(var2, unification)) {
                                        Var newvar = unification.getVar();
                                        unifiedvars.add(newvar);

                                        // Check whether all vars come from one pointcut
                                        switch (unificationType) {
                                        case -1 :
                                                if (newvar != var1) unificationType = 0; break;
                                        case 1  :
                                                if (newvar != var2) unificationType = 0; break;
                                        case 2  :
                                                if (newvar == var1) { unificationType = -1; break; }
                                                if (newvar == var2) { unificationType = 1; break; }
                                                unificationType = 0; break;
                                        }

                                } else return false;
                        }
                        if (it1.hasNext() || it2.hasNext())
                                return false;                   // The lists had different lengths

                        // COMPARING IMPLEMENTATIONS

                        if (!impl.equals(oif.getImpl())) return false;

                        // THE POINTCUTS CAN BE UNIFIED
                        // CHECK TO SEE WHETHER WE CAN REUSE EITHER this OR otherpc

                        // SANITY CHECK: if unification.unifyWithFirst(), the unification of the
                        // vars should only have succeeded if they could be unified with result
                        // the lhs var, so that the unificationType should be -1

                        if (unification.unifyWithFirst())
                                if ((unificationType != -1) && (unificationType != 2))
                                throw new RuntimeException("Unfication error: restricted unification failed (If: "+
                                        "unficationType="+unificationType+")");

                        switch (unificationType) {
                        case -1 : // All vars come from THIS
                                unification.setPointcut(this);
                                return true;
                        case 1  : // All vars come from OTHER
                                unification.setPointcut(otherpc);
                                return true;
                        case 0  : // Vars are not all from same source, need to create a new piece of syntax
                                If newpc = new If(unifiedvars,impl,jp,jpsp,ejp,getPosition());
                                unification.setPointcut(newpc);
                                return true;
                        case 2  : // No vars. Can reuse THIS
                                unification.setPointcut(this);
                                return true;
                        default : throw new RuntimeException("Invalid UnificationType "+unificationType+
                                                " in Args unification");
                        }

                } else // Do the right thing if otherpc was a local vars pc
                        return LocalPointcutVars.unifyLocals(this,otherpc,unification);

        }
}
