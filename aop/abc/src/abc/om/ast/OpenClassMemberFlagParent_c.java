/* abc - The AspectBench Compiler
 * Copyright (C) 2006
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

import polyglot.ast.Node;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import abc.aspectj.ast.ClassnamePatternExpr;

/**
 * @author Neil Ongkingco
 *
 */
public class OpenClassMemberFlagParent_c 
	extends OpenClassMemberFlag_c 
	implements OpenClassMemberFlagParent {

    ClassnamePatternExpr allowedParents;
    
    public OpenClassMemberFlagParent_c(
            ClassnamePatternExpr allowedParents, 
            Position pos) {
        super(pos);
        this.allowedParents = allowedParents;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write(" parent (");
        allowedParents.prettyPrint(w,pp);
        w.write(")");
    }

    public OpenClassMemberFlagParent_c reconstruct(ClassnamePatternExpr newAP) {
        if (this.allowedParents != newAP) {
            OpenClassMemberFlagParent_c n = 
                (OpenClassMemberFlagParent_c) copy();
            n.allowedParents = newAP;
            return n;
        }
        return this;
    }
    
    public Node visitChildren(NodeVisitor v) {
        ClassnamePatternExpr newAP = 
            (ClassnamePatternExpr) visitChild(allowedParents, v);
        return reconstruct(newAP);
    }
    
    public ClassnamePatternExpr getAllowedParents() {
        return this.allowedParents;
    }
}
