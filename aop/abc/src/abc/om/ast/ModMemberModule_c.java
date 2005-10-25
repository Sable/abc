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

import polyglot.ast.Node;
import polyglot.ast.Term;
import polyglot.ext.jl.ast.Node_c;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

/**
 * Represents a module member in the AST.
 * @author Neil Ongkingco
 *
 */
public class ModMemberModule_c extends Node_c implements ModMemberModule {

    private String name;
    private boolean isConstrained = false;
    
    public ModMemberModule_c(Position pos, String name) {
        super(pos);
        this.name = name;
    }
    
    public boolean isConstrained() {
        return isConstrained;
    }
    
    public void setIsConstrained(boolean isConstrained) {
        this.isConstrained = isConstrained;
    }
    
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write("module " + name);
        w.newline();
        //super.prettyPrint(w, pp);
    }
    
    public String name() {
        return name;
    }
    
}
