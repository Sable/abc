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
import abc.om.visit.OpenClassFlagSet.OCFType;

/**
 * @author Neil Ongkingco
 *
 */
public class MSOpenClassMemberAnd extends MSOpenClassMemberBinary {
    public MSOpenClassMemberAnd(
            MSOpenClassMember left, 
            MSOpenClassMember right) {
        super(left,right);
    }

    public boolean isAllowed(OCFType type, MSOpenClassContext context) {
        return left.isAllowed(type,context) && right.isAllowed(type,context);
    }
    
    public String toString() {
        return "(" + left.toString() + ")" + " && " + "(" + right.toString() + ")";
    }
    
    
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write("(");
        w.begin(4);
        w.newline();
        left.prettyPrint(w, pp);
        w.newline();
        w.write("&&");
        w.newline();
        right.prettyPrint(w, pp);
        w.end();
        w.newline();
        w.write(")");
    }
}
