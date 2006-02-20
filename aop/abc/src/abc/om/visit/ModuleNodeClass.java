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
 * Created on Jul 29, 2005
 *
 */
package abc.om.visit;

import abc.aspectj.ast.ClassnamePatternExpr;
import polyglot.util.Position;

/**
 * Internal representation of class members.
 * @author Neil Ongkingco
 *
 */
public class ModuleNodeClass extends ModuleNode {
    private ClassnamePatternExpr cpe;//for TYPE_CLASS nodes

    public ModuleNodeClass(String parentName, ClassnamePatternExpr cpe, Position pos) {
        this.cpe = cpe;
        this.pos = pos;
        //name is the class expression itself, prepended with the module name
        //to make sure there are no clashes in the hashmap
        this.name = parentName + "." + cpe.toString();
    }
    
    public ClassnamePatternExpr getCPE() {
        return cpe;
    }
    
    public boolean isAspect() {
        return false;
    }
    public boolean isClass() {
        return true;
    }
    public boolean isModule() {
        return false;
    }
    public int type() {
        return ModuleNode.TYPE_CLASS;
    }
}
