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
import java.util.ListIterator;
import java.util.Set;

import polyglot.types.SemanticException;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import soot.SootMethod;
import abc.weaving.matching.MatchingContext;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.AlwaysMatch;
import abc.weaving.residues.AndResidue;
import abc.weaving.residues.ContextValue;
import abc.weaving.residues.NeverMatch;
import abc.weaving.residues.Residue;

/** Handler for <code>args</code> condition pointcut.
 *  @author Aske Simon Christensen
 *  @author Ganesh Sittampalam
 *  @author Damien Sereni
 *  @author Eric Bodden
 */
public class Args extends DynamicValuePointcut {
    private List/*<ArgPattern>*/ args;

    /** Create an <code>args</code> pointcut.
     *  @param args a list of {@link abc.weaving.aspectinfo.ArgPattern} objects
     */
    public Args(List args,Position pos) {
        super(pos);
        this.args = args;
    }

    /** Get the list of argument patterns.
     *  @return a list of {@link abc.weaving.aspectinfo.ArgPattern} objects
     */
    public List getArgs() {
        return args;
    }

    public String toString() {
        StringBuffer out=new StringBuffer("args(");
        Iterator it=args.iterator();
        while(it.hasNext()) {
            out.append(it.next());
            if(it.hasNext()) out.append(",");
        }
        out.append(")");

        return out.toString();
    }

    public Pointcut inline(Hashtable renameEnv,
                              Hashtable typeEnv,
                              Aspect context,
			      int cflowdepth) {

        Iterator it=args.iterator();
        List newargs=new LinkedList();
        while(it.hasNext()) {
            ArgPattern arg=(ArgPattern) it.next();
            // Ought to delegate this really, but this is easier
            if(arg instanceof ArgVar) {
                ArgVar argvar=(ArgVar) arg;
                newargs.add(new ArgVar(argvar.getVar().rename(renameEnv),
                                       argvar.getPosition()));
            } else newargs.add(arg);
        }
        return new Args(newargs,getPosition());

    }

    public Residue matchesAt(MatchingContext mc)
        throws SemanticException
    {
        WeavingEnv we = mc.getWeavingEnv();
        SootMethod method = mc.getSootMethod();
        ShadowMatch sm = mc.getShadowMatch();
        
        if(abc.main.Debug.v().showArgsMatching)
                System.out.println("args="+args+"sm="+sm+" of type "+sm.getClass());
        Residue ret=AlwaysMatch.v();
        ListIterator formalsIt=args.listIterator();
        List actuals=sm.getArgsContextValues();
        if(abc.main.Debug.v().showArgsMatching)
            System.out.println("actuals are "+actuals);
        ListIterator actualsIt=actuals.listIterator();
        int fillerpos=-1;
        while(formalsIt.hasNext() && actualsIt.hasNext()) {
            ArgPattern formal=(ArgPattern) formalsIt.next();
            if(abc.main.Debug.v().showArgsMatching)
                System.out.println("formal is "+formal);
            if(formal instanceof ArgFill) {
                if(abc.main.Debug.v().showArgsMatching)
                    System.out.println("filler at position "+(formalsIt.nextIndex()-1)
                                       +" ("+formal.getPosition()+")");
                fillerpos=formalsIt.nextIndex();  // The position _after_ the filler
                while(formalsIt.hasNext()) formalsIt.next();
                while(actualsIt.hasNext()) actualsIt.next();
                break;
            }
            ContextValue actual=(ContextValue) actualsIt.next();

            if(abc.main.Debug.v().showArgsMatching)
                System.out.println("matching "+formal+" with "+actual);
            ret=AndResidue.construct(ret,formal.matchesAt(we,actual));

        }
        if(fillerpos==-1) {
            // we stopped because one list or the other ended,
            // and there were no ArgFills
            if(actualsIt.hasNext() ||
               (formalsIt.hasNext() &&
                // If there is one more formal left, it's ok as long as it is
                // an ArgFill. Note that we rely on the short-circuiting and
                // the left-to-right evaluation order
                // and that Iterator.next() affects the result of Iterator.hasNext()
                !(formalsIt.next() instanceof ArgFill && !formalsIt.hasNext())))
                return NeverMatch.v(); // the list lengths don't match up
            else return ret;
        }
        if(abc.main.Debug.v().showArgsMatching)
            System.out.println("actuals length is "+actuals.size()+" formals length is "+args.size());
        // There was an ArgFill
        if(actuals.size()<args.size()-1) // There aren't enough actuals for the formals minus the ArgFill
            return NeverMatch.v();

        while(formalsIt.hasPrevious() && actualsIt.hasPrevious()) {
            ArgPattern formal=(ArgPattern) formalsIt.previous();
            if(formal instanceof ArgFill) {
                /* this is now checked in the frontend:
                   if(formalsIt.nextIndex()+1!=fillerpos)
                   throw new SemanticException
                   ("Two fillers in args pattern",formal.getPosition());  */

                return ret; // all done!
            }
            ContextValue actual=(ContextValue) actualsIt.previous();

            if(abc.main.Debug.v().showArgsMatching)
                System.out.println("matching "+formal+" with "+actual);
            ret=AndResidue.construct(ret,formal.matchesAt(we,actual));
        }
        if(formalsIt.hasPrevious() && formalsIt.previous() instanceof ArgFill) return ret;
        // This shouldn't happen because we should find the filler before either the formals or the
        // actuals run out.
        throw new InternalCompilerError
            ("Internal error: reached the end of a args pattern list unexpectedly - "
             +"pattern was "+args+", method was "+method);
    }

    public void registerSetupAdvice
        (Aspect aspct,Hashtable/*<String,AbcType>*/ typeMap) {}

    public void getFreeVars(Set/*<Var>*/ result) {
        Iterator it=args.iterator();
        while(it.hasNext())
            ((ArgPattern) (it.next())).getFreeVars(result);
    }

    /* (non-Javadoc)
         * @see abc.weaving.aspectinfo.Pointcut#unify(abc.weaving.aspectinfo.Pointcut, java.util.Hashtable, java.util.Hashtable, abc.weaving.aspectinfo.Pointcut)
         */
        public boolean unify(Pointcut otherpc, Unification unification) {

                if (otherpc.getClass() == this.getClass()) {
                        List/*<ArgPattern>*/ otherargs = ((Args)otherpc).getArgs();
                        List/*<ArgPattern>*/ unifiedargs = new LinkedList();

                        // NOTE we unify the argpattern lists pointwise
                        // It may be possible to do something better, but not really useful

                        // unificationType: should be set to
                        //   -1 if ALL args come from THIS
                        //    1 if ALL args come from OTHER
                        //    0 if args come from both
                        //        2 if no args yet
                        int unificationType = 2;

                        Iterator it1 = args.iterator();
                        Iterator it2 = otherargs.iterator();

                        while (it1.hasNext() && it2.hasNext()) {
                                ArgPattern pat1 = (ArgPattern)it1.next();
                                ArgPattern pat2 = (ArgPattern)it2.next();
                                if (pat1.unify(pat2, unification)) {
                                        ArgPattern unifiedpat = unification.getArgPattern();
                                        unifiedargs.add(unifiedpat);    // Add the new pattern to the end of the list

                                        // Check whether all args come from one side
                                        switch (unificationType) {
                                        case -1 :
                                                if (unifiedpat != pat1) unificationType = 0; break;
                                        case 1  :
                                                if (unifiedpat != pat2) unificationType = 0; break;
                                        case 2  :
                                                if (unifiedpat == pat1) { unificationType = -1; break; }
                                                if (unifiedpat == pat2) { unificationType = 1; break; }
                                                unificationType = 0; break;
                                        }

                                } else return false;
                        }
                        if (it1.hasNext() || it2.hasNext())
                                return false;   // Lists had different lengths

                        // SANITY CHECK: if unification.unifyWithFirst(), the unification of the
                        // vars should only have succeeded if they could be unified with result
                        // the lhs var, so that the unificationType should be -1

                        if (unification.unifyWithFirst())
                                if ((unificationType != -1) && (unificationType != 2))
                                throw new RuntimeException("Unfication error: restricted unification failed (If: "+
                                        "unficationType="+unificationType+")");

                        switch (unificationType) {
                        case -1 : // All args come from THIS
                                unification.setPointcut(this);
                                return true;
                        case 1  : // All args come from OTHER
                                unification.setPointcut(otherpc);
                                return true;
                        case 0  : // Args are not all from same source, need to create a new piece of syntax
                                Args newpc = new Args(unifiedargs, getPosition());
                                unification.setPointcut(newpc);
                                return true;
                        case 2  : // No args. Can reuse THIS
                                unification.setPointcut(this);
                                return true;
                        default : throw new RuntimeException("Invalid UnificationType "+unificationType+
                                                " in Args unification");
                        }

                } else // Do the right thing if otherpc was a local vars pc
                        return LocalPointcutVars.unifyLocals(this,otherpc,unification);

        }
}
