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
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import polyglot.util.CodeWriter;
import polyglot.util.UniqueID;
import polyglot.util.Position;
import polyglot.util.TypedList;
import polyglot.util.InternalCompilerError;

import polyglot.ast.Block;
import polyglot.ast.Formal;
import polyglot.ast.TypeNode;
import polyglot.ast.Node;
import polyglot.ast.MethodDecl;
import polyglot.ast.Expr;
import polyglot.ast.Return;
import polyglot.ast.IntLit;
import polyglot.ast.CharLit;
import polyglot.ast.FloatLit;
import polyglot.ast.Local;
import polyglot.ast.Term;


import polyglot.types.Flags;
import polyglot.types.Context;
import polyglot.types.LocalInstance;
import polyglot.types.MethodInstance;
import polyglot.types.CodeInstance;
import polyglot.types.ConstructorInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.types.ClassType;
import polyglot.types.PrimitiveType;
import polyglot.types.ReferenceType;
import polyglot.types.ParsedClassType;

import polyglot.visit.AmbiguityRemover;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;
import polyglot.visit.TypeBuilder;
import polyglot.visit.CFGBuilder;

import abc.aspectj.extension.AJMethodDecl_c;

import abc.aspectj.ast.AdviceFormal_c;

import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.types.AJContext;

import abc.aspectj.visit.AspectInfoHarvester;
import abc.aspectj.visit.AspectMethods;
import abc.aspectj.visit.ContainsAspectInfo;
import abc.aspectj.visit.AspectReflectionInspect;
import abc.aspectj.visit.AspectReflectionRewrite;

import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.MethodCategory;
import abc.weaving.aspectinfo.AbcFactory;

/** 
 * An advice-body is similar to a normal method, but it can contain
 * references to special variables like <code>thisJoinPoint</code>
 * and, if it is around-advice, <code>proceed()</code>.
 * 
 *  @author Oege de Moor
 *  @author Aske Simon Christensen
 */
public abstract class AdviceBody_c extends AJMethodDecl_c
    implements AdviceDecl, ContainsAspectInfo, MakesAspectMethods
{
    /** record whether <code> thisJoinPoint </code> occurs in the advice body.
     *   set by joinPointFormals(Local) */
    protected boolean hasJoinPoint=false;
        /** record whether <code> thisJoinPointStaticPart </code> occurs in the advice body.
          *   set by joinPointFormals(Local) */
    protected boolean hasJoinPointStaticPart=false;
        /** record whether <code> thisEnclosingJoinPointStaticPart </code> occurs in the advice body.
          *   set by joinPointFormals(Local) */
    protected boolean hasEnclosingJoinPointStaticPart=false;


    protected LocalInstance thisJoinPointInstance=null;
    protected LocalInstance thisJoinPointStaticPartInstance=null;
    protected LocalInstance thisEnclosingJoinPointStaticPartInstance=null;

    protected boolean canRewriteThisJoinPoint=false;
    
    protected Set/*<CodeInstance>*/ methodsInAdvice;
 
    protected boolean isAroundAdvice;

 
    public AdviceBody_c(Position pos, Flags flags, TypeNode return_type,
                        String name, List formals, List throwTypes,
                        Block body, boolean isAroundAdvice)
    {
        super(pos, flags, return_type, name, formals, throwTypes, body);
            this.methodsInAdvice = new HashSet();
        this.isAroundAdvice = isAroundAdvice;
    }

    public NodeVisitor disambiguateEnter(AmbiguityRemover ar) throws SemanticException
    {
        if (ar.kind() == AmbiguityRemover.SUPER)
            return ar.bypassChildren(this);
        else if (ar.kind() == AmbiguityRemover.SIGNATURES && body != null)
            return ar.bypass(body);

        return ar;
    }
         
    protected Expr dummyVal(AJNodeFactory nf, Type t) {
        if (t instanceof ReferenceType) 
            return nf.NullLit(position());
        if (t instanceof PrimitiveType) {
            PrimitiveType pt = (PrimitiveType) t;
            if (pt.isChar())
                return nf.CharLit(position(),'x');
            if (pt.isBoolean())
                return nf.BooleanLit(position(),true);
            if (pt.isByte())
                return nf.IntLit(position(),IntLit.INT,0);
            if (pt.isShort())
                return nf.IntLit(position(),IntLit.INT,0);
            if (pt.isInt())
                return nf.IntLit(position(),IntLit.INT,0);
            if (pt.isLong())
                return nf.IntLit(position(),IntLit.LONG,0);
            if (pt.isFloat())
                return nf.FloatLit(position(),FloatLit.FLOAT,0.0);
            if (pt.isDouble())
                return nf.FloatLit(position(),FloatLit.DOUBLE,0.0);     
            if (pt.isVoid())
                throw new InternalCompilerError("cannot create expression of void type");
            else return null;
        } else return null;
    }
    
    public MethodDecl proceedDecl(AJNodeFactory nf, AJTypeSystem ts)
    {
        if (isAroundAdvice) {
            TypeNode tn = (TypeNode) returnType().copy();
            List formals = new LinkedList(formals());
            Return ret;

            if (tn.type() == ts.Void())
                ret = nf.Return(position());
            else {
                Expr dummy = dummyVal(nf, tn.type());
                ret = nf.Return(position(),dummy);
            }

            Block bl = nf.Block(position()).append(ret);
            List thrws = new LinkedList(); 
            String name = UniqueID.newID("proceed");
            MethodDecl md = nf.MethodDecl(position(),
                                Flags.PUBLIC.set(Flags.FINAL).Static(),
                                tn, name, formals, thrws, bl);      
            MethodInstance mi =
                ts.methodInstance(position(),
                                  methodInstance().container(),
                                  Flags.PUBLIC.set(Flags.FINAL).Static(),
                                  tn.type(), name,
                                  new ArrayList(methodInstance().formalTypes()),
                                  new ArrayList());
            ((ParsedClassType) methodInstance().container()).addMethod(mi);
            md = md.methodInstance(mi);
            return md;
        } else
            return null;
    }
    
    
    public boolean hasJoinPointStaticPart() {
        return hasJoinPointStaticPart;
    }
    
    public boolean hasJoinPoint() {
        return hasJoinPoint;
    }
    
    public boolean hasEnclosingJoinPointStaticPart() {
        return hasEnclosingJoinPointStaticPart;
    }
    
    public void joinpointFormals(Local n) {
        hasJoinPoint = hasJoinPoint || (n.name().equals("thisJoinPoint"));
        hasJoinPointStaticPart =
                        hasJoinPointStaticPart
                    || (n.name().equals("thisJoinPointStaticPart"));
        hasEnclosingJoinPointStaticPart =
                        hasEnclosingJoinPointStaticPart
                    || (n.name().equals("thisEnclosingJoinPointStaticPart"));
    }
 
    protected void addExtraFormals(AJNodeFactory nf, AJTypeSystem ts,
                                    List formals, List formalTypes)
    {
        // Add joinpoint parameters
        if (hasJoinPointStaticPart()) {
            TypeNode tn =
                nf.CanonicalTypeNode(position(), ts.JoinPointStaticPart())
                  .type(ts.JoinPointStaticPart());
            Formal jpsp = nf.Formal(position(), Flags.FINAL, tn,
                                    "thisJoinPointStaticPart");

            LocalInstance li = thisJoinPointStaticPartInstance(ts);
            jpsp = jpsp.localInstance(li);
            formals.add(jpsp);
            formalTypes.add(ts.JoinPointStaticPart());
        }
        if (hasJoinPoint()) {
            TypeNode tn = nf.CanonicalTypeNode(position(), ts.JoinPoint())
                            .type(ts.JoinPoint());
            Formal jp = nf.Formal(position(), Flags.FINAL, tn,
                                    "thisJoinPoint");

            LocalInstance li = thisJoinPointInstance(ts);
            jp = jp.localInstance(li);
            formals.add(jp);
            formalTypes.add(ts.JoinPoint());
        }
        if (hasEnclosingJoinPointStaticPart())
        {
            TypeNode tn =
                nf.CanonicalTypeNode(position(),ts.JoinPointStaticPart())
                  .type(ts.JoinPointStaticPart());
            Formal jp = nf.Formal(position(), Flags.FINAL, tn,
                                    "thisEnclosingJoinPointStaticPart");

            LocalInstance li = thisEnclosingJoinPointStaticPartInstance(ts);
            jp = jp.localInstance(li);
            formals.add(jp);
            formalTypes.add(ts.JoinPoint());
        }
    }

    public MethodDecl methodDecl(AJNodeFactory nf, AJTypeSystem ts)
    {
        List newformals = new LinkedList(formals());
        List newformalTypes = new LinkedList(formals());

        addExtraFormals(nf, ts, newformals, newformalTypes);

        Flags f = this.flags().set(Flags.FINAL).set(Flags.PUBLIC);
        MethodDecl md = reconstruct(returnType(), newformals, throwTypes(),
                            body()).flags(f);
        MethodInstance mi = md.methodInstance()
                              .formalTypes(newformalTypes)
                              .flags(f);
        return md.methodInstance(mi);
    }

   
    private LocalInstance thisJoinPointInstance(AJTypeSystem ts)
    {
        if (thisJoinPointInstance==null)
            thisJoinPointInstance = ts.localInstance(position(), Flags.FINAL,
                                            ts.JoinPoint(), "thisJoinPoint");
        return thisJoinPointInstance;
    }
    
    private LocalInstance thisJoinPointStaticPartInstance(AJTypeSystem ts)
    {
        if (thisJoinPointStaticPartInstance==null)
            thisJoinPointStaticPartInstance =
                ts.localInstance(position(), Flags.FINAL,
                                    ts.JoinPointStaticPart(),
                                    "thisJoinPointStaticPart");

        return thisJoinPointStaticPartInstance;
    }
         
    private LocalInstance thisEnclosingJoinPointStaticPartInstance(AJTypeSystem ts)
    {
        if (thisEnclosingJoinPointStaticPartInstance==null)
            thisEnclosingJoinPointStaticPartInstance =
                ts.localInstance(position(), Flags.FINAL,
                                    ts.JoinPointStaticPart(),
                                    "thisEnclosingJoinPointStaticPart");

        return thisEnclosingJoinPointStaticPartInstance;
    }
        
         
    public Context enterScope(Context c)
    {
        Context nc = super.enterScope(c);
        AJContext nnc = ((AJContext) nc).pushAdvice(isAroundAdvice);
        return nnc;
    }
                
    public Context enterScope(Node child, Context c) {
        if (child==body)
        {
            AJContext ajc = (AJContext) child.del().enterScope(c);
            AJTypeSystem ts = (AJTypeSystem)ajc.typeSystem();
            LocalInstance jp = thisJoinPointInstance(ts);
            ajc.addVariable(jp);
            LocalInstance sjp = thisJoinPointStaticPartInstance(ts);
            ajc.addVariable(sjp);
            LocalInstance ejpsp = thisEnclosingJoinPointStaticPartInstance(ts);
            ajc.addVariable(ejpsp);
            
            return ajc;
        }

        return super.enterScope(child,c);
    }

    /** Type check the advice: first the usual method checks, then whether the "throwing" result is
     *  actually throwable
     */
    public Node typeCheck(TypeChecker tc) throws SemanticException
    {
        super.typeCheck(tc);

        Flags f = flags().clear(Flags.STRICTFP).clear(Flags.SYNCHRONIZED);
        if (!f.equals(Flags.NONE))
            throw new SemanticException("advice cannot have flags " + f,
                                        position());
          
        return this;
    }

    protected abstract String adviceSignature();

    /** build the type; the spec is included in the advice instance to give
     *  intelligible error messages - see adviceInstance overrides
     */
    public Node buildTypes(TypeBuilder tb) throws SemanticException
    {
        TypeSystem ts = tb.typeSystem();

        List l = new ArrayList(formals.size());
        for (int i = 0; i < formals.size(); i++)
            l.add(ts.unknownType(position()));

        List m = new ArrayList(throwTypes().size());

        for (int i = 0; i < throwTypes().size(); i++)
            m.add(ts.unknownType(position()));

        MethodInstance mi = ((AJTypeSystem)ts).adviceInstance(position(),
                                    ts.Object(), Flags.NONE,
                                    ts.unknownType(position()),
                                    name, l, m, adviceSignature());

        return methodInstance(mi);
    }

    protected MethodInstance makeMethodInstance(ClassType ct, TypeSystem ts)
                                throws SemanticException
    {
        List argTypes = new LinkedList();
        List excTypes = new LinkedList();

        for (Iterator i = formals.iterator(); i.hasNext(); ) {
            Formal f = (Formal) i.next();
            argTypes.add(f.declType());
        }

        for (Iterator i = throwTypes().iterator(); i.hasNext(); ) {
            TypeNode tn = (TypeNode) i.next();
            excTypes.add(tn.type());
        }

        Flags flags = this.flags;

        if (ct.flags().isInterface())
            flags = flags.Public().Abstract();
        
        
        return ((AJTypeSystem)ts).adviceInstance(position(),
                                    ct, flags, returnType.type(), name,
                                    argTypes, excTypes, adviceSignature());
    }
                
    public abstract void prettyPrint(CodeWriter w, PrettyPrinter tr);
                
    public void localMethod(CodeInstance ci)
    {
        methodsInAdvice.add(ci);
    }

    public void aspectMethodsEnter(AspectMethods visitor)
    {
        visitor.pushProceedFor(this);
        visitor.pushFormals(formals());
        visitor.pushAdvice(this);
    }

    public Node aspectMethodsLeave(AspectMethods visitor, AJNodeFactory nf,
                                   AJTypeSystem ts)
    {
        MethodDecl md = visitor.proceed();

        visitor.popAdvice();
        visitor.popFormals();
        visitor.popProceed();

        if (md != null)
            visitor.addMethod(md);

        return this.methodDecl(nf,ts);
    }

    public void enterAspectReflectionInspect(AspectReflectionInspect v,
                                                Node parent)
    {
        v.enterAdvice();
    }

    public void leaveAspectReflectionInspect(AspectReflectionInspect v)
    {
        canRewriteThisJoinPoint = v.leaveAdvice();
    }

    public void enterAspectReflectionRewrite(AspectReflectionRewrite v,
                                                AJTypeSystem ts)
    {
        v.enterAdvice(canRewriteThisJoinPoint ?
                        thisJoinPointStaticPartInstance(ts) : null);
    }

    public Node leaveAspectReflectionRewrite(AspectReflectionRewrite v,
                                                AJNodeFactory nf)
    {
        v.leaveAdvice();
        return this;
    }
    
    public Term entry()
    {
        return listEntry(formals(), body()==null ? this : body().entry());
    }
}
