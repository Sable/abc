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

/*
 * Created on May 13, 2005
 *
 */
package abc.om.ast;

import java.util.List;

import abc.aspectj.ast.*;

import polyglot.ast.Node;
import polyglot.ast.Term;
import polyglot.ext.jl.ast.*;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

/**
 * Represents a class member in the AST.
 * 
 * @author Neil Ongkingco
 */
public class ModMemberClass_c extends Node_c implements ModMemberClass {

    private ClassnamePatternExpr cpExpr;

    public ModMemberClass_c(Position pos, ClassnamePatternExpr cpExpr) {
        super(pos);
        this.cpExpr = cpExpr;
    }

    public ClassnamePatternExpr getCPE() {
        return cpExpr;
    }
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write("class ");
        cpExpr.prettyPrint(w, pp);
        w.newline();
    }

    private ModMemberClass_c reconstruct(ClassnamePatternExpr cpExpr) {
        if (cpExpr != this.cpExpr) {
            ModMemberClass_c n = (ModMemberClass_c) copy();
            n.cpExpr = cpExpr;
            return n;
        }
        return this;
    }

    public Node visitChildren(NodeVisitor v) {

        ClassnamePatternExpr cpe = (ClassnamePatternExpr) visitChild(cpExpr, v);
        
        return reconstruct(cpe);
    }

}
