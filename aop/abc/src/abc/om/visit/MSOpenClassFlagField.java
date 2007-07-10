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
import abc.om.ast.OpenClassMemberFlag;
import abc.om.ast.OpenClassMemberFlagField;

/**
 * @author Neil Ongkingco
 *
 */
public class MSOpenClassFlagField extends MSOpenClassFlag {
    
    public MSOpenClassFlagField(OpenClassMemberFlag member) {
        super();
        assert (member instanceof OpenClassMemberFlagField) : "Incorrect parameter type";
    }
    
    public MSOpenClassFlag conjoin(MSOpenClassFlag other) {
        if (other == null) {
            return null;
        }
        return this;
    }
    public MSOpenClassFlag disjoin(MSOpenClassFlag other) {
        return this;
    }
    
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write("field");
    }
    
    public String toString() {
        return "field";
    }
    public boolean isAllowed(MSOpenClassContext context) {
        MSOpenClassContextField fieldContext =
            (MSOpenClassContextField) context;
        return true;
    }
}
