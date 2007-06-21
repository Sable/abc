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
 *
 * This class is for compiler-generated advice for tracematches,
 * where the advice is executed once for each event (that is,
 * any joinpoint where at least one symbol matches).
 *
 * Since symbols can match before and after joinpoints, this
 * advice is passed two pointcuts - one for before, and one
 * for after. One of these pointcuts can be null, but not
 * both, and they cannot have any free pointcut variables.
 */
public class PerEventAdviceDecl_c extends AdviceBody_c
                                  implements TMAdviceDecl
{
    protected int kind; /* TMAdviceDecl . SOME / SYNC */
    protected String tm_id;
    protected Position tm_pos;

    protected AdviceSpec before_spec;
    protected Pointcut before_pc;

    protected AdviceSpec after_spec;
    protected Pointcut after_pc;

    public PerEventAdviceDecl_c(Position pos, Flags flags,
                                AdviceSpec before_spec, Pointcut before_pc,
                                AdviceSpec after_spec, Pointcut after_pc,
                                Block body, String tm_id,
                                Position tm_pos, int kind)
    {
        super(pos, flags, before_spec.returnType(),
                UniqueID.newID("beforeafter"), new LinkedList(),
                new LinkedList(), body, false);

        this.kind = kind;
        this.tm_id = tm_id;
        this.tm_pos = tm_pos;

        this.before_spec = before_spec;
        this.before_pc = before_pc;
        this.after_spec = after_spec;
        this.after_pc = after_pc;

        if (before_pc == null && after_pc == null)
            throw new InternalCompilerError(
                        "Per-event advice created with two null pointcuts");
    }
 
    protected PerEventAdviceDecl_c
                    reconstruct(TypeNode returnType, List formals,
                                List throwTypes, Pointcut before_pc,
                                Pointcut after_pc, Block body)
    {
        PerEventAdviceDecl_c n = this;

        if (before_pc != this.before_pc || after_pc != this.after_pc) {
            n = (PerEventAdviceDecl_c) copy();
            n.before_pc = before_pc;
            n.after_pc = after_pc;
        }

        return (PerEventAdviceDecl_c)
                    n.reconstruct(returnType, formals, throwTypes, body);
    }

    public Node visitChildren(NodeVisitor v)
    {
        TypeNode returnType = (TypeNode) visitChild(this.returnType, v);
        List formals = visitList(this.formals, v);
        List throwTypes = visitList(this.throwTypes, v);

        Pointcut before_pc = null;
        if (this.before_pc != null)
            before_pc = (Pointcut) visitChild(this.before_pc, v);

        Pointcut after_pc = null;
        if (this.after_pc != null)
            after_pc = (Pointcut) visitChild(this.after_pc, v);

        Block body = (Block) visitChild(this.body, v);

        return reconstruct(returnType, formals, throwTypes,
                           before_pc, after_pc, body);
    }

    public NodeVisitor disambiguateEnter(AmbiguityRemover ar) throws SemanticException
    {
        if (ar.kind() == AmbiguityRemover.SUPER) {
            return ar.bypassChildren(this);
        }
        else if (ar.kind() == AmbiguityRemover.SIGNATURES) {
            if (body != null) {
                Collection bp = new LinkedList();
                bp.add(body);
                if (before_pc != null)
                    bp.add(before_pc);
                if (after_pc != null)
                    bp.add(after_pc);
                return ar.bypass(bp);
            }
        }

        return ar;
    }
         
    protected String adviceSignature()
    {
        return "beforeafter()";
    }

    /** Type check the advice: first the usual method checks,
     *  then pointcut checks.
     */
    public Node typeCheck(TypeChecker tc) throws SemanticException
    {
        Node n = super.typeCheck(tc);
        if (before_pc != null)
            before_pc.checkFormals(formals);
        if (after_pc != null)
            after_pc.checkFormals(formals);
        return n;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr)
    {
        w.begin(0);
        w.write(flags.translate());

        if (before_pc != null) {
            print(before_spec, w, tr);
            w.write(":");
            print(before_pc, w, tr);
        }

        w.allowBreak(0);

        if (after_pc != null) {
            print(after_spec, w, tr);
            w.write(":");
            print(after_pc, w, tr);
        }

        w.allowBreak(0);
        
        if (body != null) 
            printSubStmt(body, w, tr);
        else
            w.write(";");

        w.end();
    }
                
    public void update(GlobalAspectInfo gai, Aspect current_aspect)
    {
        int lastpos = formals().size();
        int jp = -1, jpsp = -1, ejp = -1;

        if (hasEnclosingJoinPointStaticPart) ejp = --lastpos;
        if (hasJoinPoint) jp = --lastpos;
        if (hasJoinPointStaticPart) jpsp = --lastpos;

        before_spec.setReturnType(returnType());
        after_spec.setReturnType(returnType());

        List methods = new ArrayList();
        for (Iterator procs = methodsInAdvice.iterator(); procs.hasNext(); )
        {
            CodeInstance ci = (CodeInstance) procs.next();

            if (ci instanceof MethodInstance)
                methods.add(AbcFactory.MethodSig((MethodInstance) ci));
            if (ci instanceof ConstructorInstance)
                methods.add(AbcFactory.MethodSig((ConstructorInstance) ci));
        }

        if (before_pc != null) {
            abc.weaving.aspectinfo.AdviceDecl before_ad =
                new abc.tm.weaving.aspectinfo.TMAdviceDecl
                    (before_spec.makeAIAdviceSpec(),
                     before_pc.makeAIPointcut(),
                     AbcFactory.MethodSig(this),
                     current_aspect,
                     jp, jpsp, ejp, methods,
                     position(), tm_id, tm_pos, kind);

            gai.addAdviceDecl(before_ad);
        }

        if (after_pc != null) {
            abc.weaving.aspectinfo.AdviceDecl after_ad =
                new abc.tm.weaving.aspectinfo.TMAdviceDecl
                    (after_spec.makeAIAdviceSpec(),
                     after_pc.makeAIPointcut(),
                     AbcFactory.MethodSig(this),
                     current_aspect,
                     jp, jpsp, ejp, methods,
                     position(), tm_id, tm_pos, kind);

            gai.addAdviceDecl(after_ad);
        }

        // don't advise this method or calls to it
        MethodCategory.register(this, MethodCategory.NO_EFFECTS_ON_BASE_CODE);
    }
}
