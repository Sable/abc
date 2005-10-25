/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Neil Ongkingco
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

package abc.om.ast;

import java.util.Collections;
import java.util.LinkedList;

import abc.aspectj.ast.AJNodeFactory;
import abc.aspectj.ast.CPEUniversal_c;
import abc.aspectj.ast.ClassnamePatternExpr;
import abc.aspectj.ast.ClassnamePatternExpr_c;
import abc.aspectj.ast.MakesAspectMethods;
import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.visit.AspectMethods;
import abc.om.AbcExtension;
import abc.om.weaving.aspectinfo.OMClassnamePattern;
import abc.om.weaving.aspectinfo.ThisAspectPointcut;
import abc.weaving.aspectinfo.AndPointcut;
import abc.weaving.aspectinfo.ClassnamePattern;
import abc.weaving.aspectinfo.Pointcut;
import polyglot.ast.Node;
import polyglot.ext.jl.ast.Node_c;
import polyglot.util.CodeWriter;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

/**
 * An abstract signature member. Extended by exposeDecl and advertiseDecl
 * @author Neil Ongkingco
 *
 */
public abstract class SigMember_c extends Node_c implements MakesAspectMethods{
    protected abc.aspectj.ast.Pointcut pc;
    protected boolean isPrivate = false;
    ClassnamePatternExpr toClause;
    
    public SigMember_c(polyglot.util.Position pos, 
            abc.aspectj.ast.Pointcut pc, 
            boolean isPrivate, 
            ClassnamePatternExpr toClauseCPE) {
        super(pos);
        this.pc = pc;
        this.isPrivate = isPrivate;
        toClause = toClauseCPE;
    }
    
    public boolean isPrivate() {
        return isPrivate;
    }
    
    public Pointcut getAIPointcut() {
        ClassnamePattern cp = new OMClassnamePattern(toClause);
        return AndPointcut.construct(
                this.pc.makeAIPointcut(), 
                ThisAspectPointcut.construct(cp, 
                        AbcExtension.generated), 
                AbcExtension.generated);
    }
    
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        pc.prettyPrint(w, pp);
        w.newline();
    }    
    
    public SigMember_c reconstruct(abc.aspectj.ast.Pointcut pc, 
            ClassnamePatternExpr toClause) {
        if (pc != this.pc || toClause != toClause) {
            SigMember_c n = (SigMember_c)copy();
            n.pc = pc;
            return n;
        }
        return this;
    }
    
    public Node visitChildren(NodeVisitor v) {
        abc.aspectj.ast.Pointcut pc = 
            (abc.aspectj.ast.Pointcut)visitChild(this.pc, v);
        ClassnamePatternExpr toClause = 
            (ClassnamePatternExpr)visitChild(this.toClause, v);
        return reconstruct(pc, toClause);
    }

    public void aspectMethodsEnter(AspectMethods visitor) {
        //push an empty list of formals. needed for If pointcuts
        visitor.pushFormals(Collections.unmodifiableList(new LinkedList()));
    }
    public Node aspectMethodsLeave(AspectMethods visitor, AJNodeFactory nf,
            AJTypeSystem ts) {
        return this;
    }
}
