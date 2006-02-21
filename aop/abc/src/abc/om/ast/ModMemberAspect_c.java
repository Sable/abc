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

import abc.aspectj.ast.CPEName;
import abc.aspectj.ast.ClassnamePatternExpr;
import abc.aspectj.ast.NamePattern;
import abc.aspectj.ast.SimpleNamePattern_c;
import abc.aspectj.visit.ContainsNamePattern;

import polyglot.ast.Node;
import polyglot.ast.Term;
import polyglot.ext.jl.ast.Node_c;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

/**
 * Represents an aspect member in the AST.
 * 
 * @author Neil Ongkingco
 */
public class ModMemberAspect_c extends Node_c implements ModMemberAspect,
        ContainsNamePattern {

    private String name;
    private NamePattern namePattern;
    private CPEName cpe;

    public ModMemberAspect_c(Position pos, CPEName cpe) {
        super(pos);
        this.name = cpe.getNamePattern().toString();
        this.cpe = cpe;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write("aspect " + name);
        w.newline();
    }

    public String name() {
        return name;
    }
    
    public NamePattern getNamePattern() {
        return cpe.getNamePattern();
    }
    
    public ClassnamePatternExpr getCPE() {
        return cpe;
    }
    
    private ModMemberAspect_c reconstruct(NamePattern namePattern) {
        if (namePattern != this.namePattern) {
            ModMemberAspect_c n = (ModMemberAspect_c) copy();
            n.name = this.name;
            n.namePattern = namePattern;
            return n;
        }
        return this;
    }

    public Node visitChildren(NodeVisitor v) {

        NamePattern namePattern = (NamePattern) visitChild(this.namePattern, v);
        
        return reconstruct(namePattern);
    }

}
