/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Julian Tibble
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

package abc.eaj.visit;

import java.util.*;

import polyglot.ast.*;
import polyglot.frontend.*;
import polyglot.visit.*;

import abc.aspectj.ast.*;
import abc.aspectj.visit.*;

import abc.eaj.ast.*;
import abc.eaj.extension.*;
import abc.eaj.types.*;

/**
 * @author Julian Tibble
 */
public class GlobalPointcuts extends ContextVisitor
{
    public final static int COLLECT = 1;
    public final static int CONJOIN = 2;

    // This visitor must maintain state in between jobs.
    // The mapping from aspect patterns to global
    // pointcuts is therefore kept in a static variable
    //
    // The visitor is also run in two stages COLLECT,
    // and CONJOIN. Since these are separated by a
    // global barrier pass, we can use a static counter
    // to determine when the mapping should be
    // re-initialised.
    static HashMap /*ClassnamePatternExpr,Pointcut*/ globalpcs = new HashMap();
    static int unmatchedCollectPasses = 0;


    EAJNodeFactory nodeFactory;
    int pass;

    public GlobalPointcuts(int pass, Job job, EAJTypeSystem ts, EAJNodeFactory nf)
    {
        super(job, ts, nf);
        this.nodeFactory = nf;
        this.pass = pass;
    }

    /**
     * callback to allow a GlobalPoincutDecl to register itself
     */
    public void addGlobalPointcut(ClassnamePatternExpr pattern,
                                  Pointcut pc)
    {
        if (globalpcs.containsKey(pattern)) {
            Pointcut current = (Pointcut) globalpcs.get(pattern);
            globalpcs.put(pattern, conjoinPointcuts(pc, current));
        } else {
            globalpcs.put(pattern, pc);
        }
    }

    public Pointcut conjoinPointcuts(Pointcut a, Pointcut b)
    {
        return nodeFactory.PCBinary(b.position(), a, PCBinary.COND_AND, b);
    }


    // Methods implementing ContextVisitor interface

    // maintain the static state
    public void finish()
    {
        switch (pass) {
            case COLLECT:
                unmatchedCollectPasses++;
                break;
            case CONJOIN:
                unmatchedCollectPasses--;
        }

        if (unmatchedCollectPasses == 0)
            globalpcs = new HashMap();
    }

    public NodeVisitor enter(Node parent, Node n)
    {
        if (pass == COLLECT && n instanceof GlobalPointcutDecl) {
            ((GlobalPointcutDecl) n).registerGlobalPointcut(this, context(),
                                                            nodeFactory);
        }
        return super.enter(parent, n);
    }

    public Node leave(Node parent, Node old, Node n, NodeVisitor v)
    {
        n = super.leave(parent, old, n, v);

        if (pass == CONJOIN && n instanceof EAJAdviceDecl) {
            EAJAdviceDecl adviceDecl = (EAJAdviceDecl) n;
            PCNode aspct = PCStructure.v().getClass(context().currentClass());

            return applyMatchingGlobals(aspct, adviceDecl);
        }

        return n;
    }

    protected EAJAdviceDecl applyMatchingGlobals(PCNode aspct,
                                                 EAJAdviceDecl ad)
    {
        Iterator i = globalpcs.keySet().iterator();

        while (i.hasNext()) {
            ClassnamePatternExpr pattern = (ClassnamePatternExpr) i.next();

            if (pattern.matches(aspct)) {
                Pointcut global = (Pointcut) globalpcs.get(pattern);
                ad = ad.conjoinPointcutWith(this, global);
            }
        }

        return ad;
    }
}
