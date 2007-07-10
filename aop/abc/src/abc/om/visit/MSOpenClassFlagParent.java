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
package abc.om.visit;

import polyglot.util.CodeWriter;
import polyglot.visit.PrettyPrinter;
import abc.aspectj.ast.ClassnamePatternExpr;
import abc.om.ast.OpenClassMemberFlag;
import abc.om.ast.OpenClassMemberFlagParent;

/**
 * @author Neil Ongkingco
 *
 */
public class MSOpenClassFlagParent extends MSOpenClassFlag {
    
    ClassnamePatternExpr allowedParents;
    
    public MSOpenClassFlagParent(OpenClassMemberFlag member) {
        super();
        OpenClassMemberFlagParent parentMember = (OpenClassMemberFlagParent) member;
        this.allowedParents = parentMember.getAllowedParents();
    }
    
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write("parent(");
        allowedParents.prettyPrint(w, pp);
        w.write(")");
    }
    
    public String toString() {
        return "parent(" + allowedParents.toString() + ")";
    }
    
    public boolean isAllowed(MSOpenClassContext context) {
        //check if parent allowed
        MSOpenClassContextParent parentContext = 
            (MSOpenClassContextParent) context;
        boolean result = allowedParents.matches(parentContext.getParentNode());
        return result;
    }
}
