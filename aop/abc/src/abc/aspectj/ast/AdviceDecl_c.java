/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Oege de Moor
 * Copyright (C) 2004 Aske Simon Christensen
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

package abc.aspectj.ast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.Formal;
import polyglot.ast.MethodDecl;
import polyglot.ast.Node;
import polyglot.ast.Term;
import polyglot.ast.TypeNode;
import polyglot.types.CodeInstance;
import polyglot.types.ConstructorInstance;
import polyglot.types.Context;
import polyglot.types.Flags;
import polyglot.types.MethodInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.UniqueID;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;
import abc.aspectj.types.AJContext;
import abc.aspectj.types.AJTypeSystem;
import abc.weaving.aspectinfo.AbcFactory;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.MethodCategory;

/** 
 * 
 * Declarations of advice, for example
 * 
 *  <code> 
 *      before(int x)      // the "advice spec"
 *    : 
 *      call(* fac(*)) && args(x)  // pointcut
 *    { System.out.println(x);} // body
 *  </code>
 *  @author Oege de Moor
 *  @author Aske Simon Christensen
 */
public class AdviceDecl_c extends AdviceBody_c
{
        /** advice specification 
         *  (e.g. <code> before(formals), around(..), after returning(..) </code>) */
    protected AdviceSpec spec;
    
    /** pointcut that specifies the joinpoints 
     *  where this advice applies */
    protected Pointcut pc;
    
    /** the return formal, for
     *  <code> afterreturning </code> and 
     *  <code> afterthrowing </code> advice */
    protected AdviceFormal retval;
    
    public AdviceDecl_c(Position pos, Flags flags, AdviceSpec spec,
                        List throwTypes, Pointcut pc, Block body)
    {
        super(pos, flags, spec.returnType(), UniqueID.newID(spec.kind()),
                spec.formals(), throwTypes, body, spec instanceof Around);
        this.spec = spec;
        this.pc = pc;
        this.retval = spec.returnVal();
    }
    
    
    // new visitor code
    protected AdviceDecl_c reconstruct(TypeNode returnType, List formals,
                                        List throwTypes, Block body,
                                        AdviceSpec spec, AdviceFormal retval,
                                        Pointcut pc)
    {
        if (spec != this.spec || pc != this.pc || retval != this.retval) {
            AdviceDecl_c n = (AdviceDecl_c) copy();
            n.spec = spec;
            n.pc = pc;
            n.retval = retval;
            return (AdviceDecl_c) n.reconstruct(returnType, formals, throwTypes, body);
        }

        return (AdviceDecl_c) super.reconstruct(returnType, formals, throwTypes, body);
    }

    public Node visitChildren(NodeVisitor v)
    {
        TypeNode returnType = (TypeNode) visitChild(this.returnType, v);
        AdviceSpec spec = (AdviceSpec) visitChild(this.spec, v);
        List formals = visitList(this.formals, v);
        List throwTypes = visitList(this.throwTypes, v);
        AdviceFormal retval = (AdviceFormal) visitChild(this.retval,v);
        Pointcut pc = (Pointcut) visitChild(this.pc,v);
        Block body = (Block) visitChild(this.body, v);
        return reconstruct(returnType, formals, throwTypes, body, spec, retval, pc);
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
                bp.add(pc);
                bp.add(retval);
                return ar.bypass(bp);
            }
        }

        return ar;
    }
         
    public MethodDecl proceedDecl(AJNodeFactory nf, AJTypeSystem ts)
    {
        MethodDecl md = super.proceedDecl(nf, ts);

        if (isAroundAdvice)
            ((Around) spec).setProceed(md);

        return md;
    }
 
    protected void addExtraFormals(AJNodeFactory nf, AJTypeSystem ts,
                                List formals, List formalTypes)
    {
        if (retval != null) {
            formals.add(retval);
            formalTypes.add(retval.type());
        }

        super.addExtraFormals(nf, ts, formals, formalTypes);
    }

    protected String adviceSignature()
    {
        return spec.toString();
    }

    public Context enterScope(Node child, Context c)
    {
        AJContext ajc = (AJContext) super.enterScope(child, c);
        AJTypeSystem ts = (AJTypeSystem) ajc.typeSystem();

        if (child == body && retval != null)
            ajc.addVariable(retval.localInstance());

        if (child == body && isAroundAdvice) {
            LinkedList l = new LinkedList();
            l.add(ts.Throwable());
            MethodInstance proceedInstance =
                methodInstance().name("proceed")
                                .flags(flags().Public().Static())
                                .throwTypes(l);
            ajc.addProceed(proceedInstance);
        }

        return ajc;
    }

        
    /** Type check the advice: first the usual method checks, then whether the "throwing" result is
     *  actually throwable
     */
    public Node typeCheck(TypeChecker tc) throws SemanticException
    {
        Node n = super.typeCheck(tc);

        if (spec instanceof AfterThrowing && retval != null) {
            Type t = retval.type().type();
            if (! t.isThrowable()) {
                TypeSystem ts = tc.typeSystem();
                throw new SemanticException("type \"" + t +
                                "\" is not a subclass of \"" + ts.Throwable() +
                                "\".", spec.returnVal().type().position());
            }
        }
                
        pc.checkFormals(formals);

        return n;
    }
 

    public void prettyPrint(CodeWriter w, PrettyPrinter tr)
    {
        w.begin(0);
        w.write(flags.translate());

        print(spec,w,tr);

        w.begin(0);

        if (! throwTypes().isEmpty()) {
            w.allowBreak(6);
            w.write("throws ");

            for (Iterator i = throwTypes().iterator(); i.hasNext(); ) {
                TypeNode tn = (TypeNode) i.next();
                print(tn, w, tr);
        
                if (i.hasNext()) {
                    w.write(",");
                    w.allowBreak(4, " ");
                }
            }
        }

        w.end();

        w.write(":");

        w.allowBreak(0);

        print(pc, w, tr);
        
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

        abc.weaving.aspectinfo.AdviceDecl ad =
            new abc.weaving.aspectinfo.AdviceDecl
            (spec.makeAIAdviceSpec(),
             pc.makeAIPointcut(),
             AbcFactory.MethodSig(this),
             current_aspect,
             jp, jpsp, ejp, methods,
             position());
        gai.addAdviceDecl(ad);
 

        MethodCategory.register(this, MethodCategory.ADVICE_BODY);
        if (spec instanceof Around) {
            MethodCategory.register(((Around)spec).proceed(), MethodCategory.PROCEED);
        }
    }

    
    public Term entry()
    {
        if (retval == null)
            return super.entry();

        return listEntry(formals(), retval.entry());
    }
           
    /**
     * Visit this term in evaluation order.
     */
    public List acceptCFG(CFGBuilder v, List succs)
    {
        if (retval==null)
            return super.acceptCFG(v,succs);
        if (body() == null) {
            v.visitCFGList(formals(), retval.entry());
            v.visitCFG(retval, this);
        } else {
            v.visitCFGList(formals(), retval.entry());
            v.visitCFG(retval, body().entry());
            v.visitCFG(body(), this);
        }

        return succs;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Formal getReturnThrowsFormal() {
    	return retval;
    }
}
