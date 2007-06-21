/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Julian Tibble
 * Copyright (C) 2007 Eric Bodden
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

package abc.tm.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;

import abc.aspectj.ast.*;

import abc.weaving.aspectinfo.AbcFactory;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.MethodCategory;

import java.util.*;

/**
 * @author Julian Tibble
 * @author Eric Bodden
 *
 * This class is for compiler-generated advice for implementing
 * TraceMatches. This advice requires different type-checking and
 * precedence rules to normal advice.
 */
public class PerSymbolAdviceDecl_c extends AdviceDecl_c
                                   implements TMAdviceDecl
{
    protected String tm_id;
    protected Position tm_pos;
    protected SymbolDecl sym;

    public PerSymbolAdviceDecl_c(Position pos, Flags flags, AdviceSpec spec,
                                List throwTypes, Pointcut pc, Block body,
                                String tm_id, SymbolDecl sym, Position tm_pos)
    {
        super(pos, flags, spec, throwTypes, pc, body);
        this.tm_id = tm_id;
        this.tm_pos = tm_pos;
        assert sym != null;
        this.sym = sym;
        if (tm_pos == null)
        	System.err.println("gen advice decl with null tm pos");
    }

    public Node typeCheck(TypeChecker tc) throws SemanticException
    {
        // Remove all tracematch formals that are not used in
        // the pointcut for this piece of advice.
 
        Collection pc_locals = pc.mayBind();
        Iterator i = formals.iterator();
        List new_formals = new LinkedList();

        while (i.hasNext()) {
            Formal f = (Formal) i.next();
            if (pc_locals.contains(f.name()))
                new_formals.add(f);
        }
        formals = new_formals;

        return super.typeCheck(tc);
    }


    // FIXME: this method is huge, and the only difference from
    // super.update(...) is we instantiate the aspectinfo class
    // TMAdviceDecl instead of AdviceDecl.
    //
    // Some re-factoring is in order.
    //
    public void update(GlobalAspectInfo gai, Aspect current_aspect)
    {
        int lastpos = formals().size();
        int jp = -1, jpsp = -1, ejp = -1;
        if (hasEnclosingJoinPointStaticPart) ejp = --lastpos;
        if (hasJoinPoint) jp = --lastpos;
        if (hasJoinPointStaticPart) jpsp = --lastpos;

        // Since the spec is not visited, we copy the (checked)
        // return type node from the advice declaration
        spec.setReturnType(returnType());
        // And the return formal as well
        if (retval != null) {
            spec.setReturnVal(retval);
        }
	
        List methods = new ArrayList();
        for (Iterator procs = methodsInAdvice.iterator(); procs.hasNext(); ) {
            CodeInstance ci = (CodeInstance) procs.next();
            if (ci instanceof MethodInstance)
			methods.add(AbcFactory.MethodSig((MethodInstance)ci));
            if (ci instanceof ConstructorInstance)
            methods.add(AbcFactory.MethodSig((ConstructorInstance)ci));
        }

        abc.tm.weaving.aspectinfo.TMAdviceDecl ad =
	        new abc.tm.weaving.aspectinfo.PerSymbolTMAdviceDecl(
	            spec.makeAIAdviceSpec(),
	            pc.makeAIPointcut(),
	            AbcFactory.MethodSig(this),
	            current_aspect,
	            jp, jpsp, ejp, methods,
	            position(), tm_id, tm_pos, sym.name(), TMAdviceDecl.OTHER);

        gai.addAdviceDecl(ad);
	
        // don't advise this method or calls to it
        MethodCategory.register(this, MethodCategory.NO_EFFECTS_ON_BASE_CODE);
        if (spec instanceof Around) {
            MethodCategory.register(((Around)spec).proceed(),
                                    MethodCategory.PROCEED);
        }
    }
}
