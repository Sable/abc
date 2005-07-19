/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Julian Tibble
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

package abc.tm.visit;

import java.util.*;

import polyglot.ast.*;
import polyglot.frontend.*;
import polyglot.util.*;
import polyglot.visit.*;

import abc.aspectj.ast.*;
import abc.aspectj.visit.*;
import abc.aspectj.types.*;

import abc.tm.ast.*;

/**
 * @author Julian Tibble
 *
 * A TraceMatch uses pieces of advice to determine when the
 * current execution trace matches the TraceMatch's regular
 * expression. These pieces of advice need to be added to the
 * aspect which contains the tracematch.
 *
 * That is what this visitor does.
 */
public class MoveTraceMatchMembers extends ContextVisitor
{
    private TMNodeFactory nf;
    private AJTypeSystem ts;

    private List advice = new LinkedList();

    public MoveTraceMatchMembers(Job job, AJTypeSystem ts, TMNodeFactory nf)
    {
        super(job, ts, nf);
        this.nf = nf;
        this.ts = ts;
    }

    public Node leave(Node parent, Node old, Node n, NodeVisitor v)
    {
        n = super.leave(parent, old, n, v);

        if (n instanceof TMDecl) {
            TypeNode voidn =
                nf.CanonicalTypeNode(Position.COMPILER_GENERATED, ts.Void());
            TMDecl tmd = (TMDecl) n;
            compileAndStoreAdvice(tmd.generateImplementationAdvice(nf, voidn));
        }
        else if (n instanceof AspectBody) {
            Iterator i = advice.iterator();
            while (i.hasNext()) {
                TMAdviceDecl tm_ad = (TMAdviceDecl) i.next();
                n = ((AspectBody) n).addMember(tm_ad);
            }

            // reset the list of advice
            advice = new LinkedList();
        }

        return n;
    }

    protected void compileAndStoreAdvice(List tm_advice)
    {
        Iterator i = tm_advice.iterator();
        while (i.hasNext()) {
            TMAdviceDecl tm_ad = (TMAdviceDecl) i.next();
            advice.add(compileAdvice(tm_ad));
        }
    }

    protected TMAdviceDecl compileAdvice(TMAdviceDecl tm_ad)
    {
    	// need to run in phases, as the pattern evaluator doesn't like
    	// processing generated tree fragments
        Job compile_tm_ad1 =
            job().spawn(context(), tm_ad, Pass.BUILD_TYPES,Pass.BUILD_TYPES);
        if (!compile_tm_ad1.status()) {
            // oops, this shouldn't have produced any errors
            throw new InternalCompilerError(
                        "Compiling generated advice failed in type building");
        }
        Job compile_tm_ad2 =
        	job().spawn(context(), compile_tm_ad1.ast(),Pass.CLEAN_SIGS,Pass.DISAM_ALL);
        if (!compile_tm_ad2.status()) {
            // oops, this shouldn't have produced any errors
            throw new InternalCompilerError(
                        "Compiling generated advice failed");
        }

        return (TMAdviceDecl) compile_tm_ad2.ast();
    }
}
