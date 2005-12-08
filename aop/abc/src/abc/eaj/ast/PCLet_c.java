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

package abc.eaj.ast;

import java.util.*;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;

import abc.aspectj.ast.*;
import abc.aspectj.types.*;
import abc.aspectj.visit.*;

import abc.weaving.aspectinfo.AbcFactory;
import abc.weaving.aspectinfo.MethodCategory;

/**
 *  @author Julian Tibble
 */
public class PCLet_c extends PCIf_c implements PCLet
{
    protected Local var;

    public PCLet_c(Position pos, Local var, Expr expr)
    {
        super(pos, expr);
        this.var = var;
    }

    /**
     * visit the children of the let
     */
    public Node visitChildren(NodeVisitor v)
    {
        Local var = (Local) visitChild(this.var, v);
        PCLet_c pcl = (PCLet_c) super.visitChildren(v);

        if (this.var != var) {
            pcl = (PCLet_c) pcl.copy();
            pcl.var = var;
        }

        return pcl;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr)
    {
        w.write("let(");
        print(var, w, tr);
        w.write(", ");
        print(expr, w, tr);
        w.write(")");
    }

    public Collection mayBind() throws SemanticException
    {
        return mustBind();
    }

    public Collection mustBind()
    {
        Collection mustbind = new HashSet();
        mustbind.add(var.name());
        return mustbind;
    }

    public Node typeCheck(TypeChecker tc) throws SemanticException
    {
        Type var_type = var.type();
        Type expr_type = expr.type();

        TypeSystem ts = tc.typeSystem();

        if (! ts.isImplicitCastValid(expr_type, var_type) &&
            ! ts.equals(expr_type, var_type) &&
            ! ts.numericConversionValid(var_type, expr.constantValue()))
        {
            throw new SemanticException("Cannot bind " + expr_type +
                        " to " + var_type + ".", position());
        }

        return this;
    }

    public Type childExpectedType(Expr child, AscriptionVisitor av)
    {
        TypeSystem ts = av.typeSystem();

        if (child == expr) {
            return var.type();
        }

        return child.type();
    }

    public MethodDecl exprMethod(AJNodeFactory nf, AJTypeSystem ts,
                                 List formals, ParsedClassType container)
    {
        Return ret = nf.Return(position(), expr);
        Block bl = nf.Block(position()).append(ret);
        TypeNode retType = nf.CanonicalTypeNode(position(), var.type());

        List args = new LinkedList();
        List formaltypes = new ArrayList(); 

        // put formals into the list of args ignoring references
        // to the pointcut formal bound by this let
        Iterator i = formals.iterator();
        while (i.hasNext()) {
            Formal f = (Formal) i.next();
            if (!f.name().equals(var.name())) {
                args.add(f);
                formaltypes.add(f.type().type());
            }
        }

        List throwTypes = new LinkedList();
        i = expr.throwTypes(ts).iterator();
        while (i.hasNext()) {
            Type t = (Type) i.next();
            TypeNode tn = nf.CanonicalTypeNode(position(), t);
            throwTypes.add(tn);
        }

        addJoinPointFormals(nf, ts, args, formaltypes);

        methodName = UniqueID.newID("let");
        MethodDecl md = nf.MethodDecl(position(), Flags.STATIC.Public(),
                                  retType, methodName, args, throwTypes, bl);
        MethodInstance mi = ts.methodInstance(position, container,
                                Flags.STATIC.Public(), retType.type(),
                                methodName, new ArrayList(formaltypes),
                                new ArrayList(expr.del().throwTypes(ts)));

        container.addMethod(mi);
        md = md.methodInstance(mi);
        methodDecl = md;
        return md;
    }

    protected List calculateMethodParameters(AspectMethods visitor,
                                        AJNodeFactory nf, AJTypeSystem ts)
    {
        List formals = new ArrayList();
        Iterator i = super.calculateMethodParameters(visitor, nf, ts)
                          .iterator();

        while (i.hasNext()) {
            Formal f = (Formal) i.next();
            if (! f.name().equals(var.name()))
                formals.add(f);
        }

        return formals;
    }

    public abc.weaving.aspectinfo.Pointcut makeAIPointcut()
    {
        int lastpos = methodDecl.formals().size();
        int jp = -1, jpsp = -1, ejp = -1;
        if (hasEnclosingJoinPointStaticPart) ejp = --lastpos;
        if (hasJoinPoint) jp = --lastpos;
        if (hasJoinPointStaticPart) jpsp = --lastpos;

        MethodCategory.register(methodDecl, MethodCategory.IF_EXPR);

        List vars = new ArrayList();
        Iterator fi = methodDecl.formals().iterator();
        while (fi.hasNext()) {
            Formal f = (Formal) fi.next();
            vars.add(new abc.weaving.aspectinfo.Var(f.name(), f.position()));
        }

        

        return new abc.eaj.weaving.aspectinfo.Let
            (new abc.weaving.aspectinfo.Var(var.name(), var.position()),
             vars, AbcFactory.MethodSig(methodDecl), jp, jpsp, ejp, position);
    }
}
