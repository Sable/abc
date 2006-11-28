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

import abc.aspectj.visit.PCNode;

/**
 * @author Neil Ongkingco
 * Context used to check if permission is allowed for a given open class feature 
 */
public abstract class MSOpenClassContext {
    protected PCNode classNode = null; //PCNode of class being applied to
    protected PCNode aspectNode = null; //PCNode of the aspect applying the feature
    
    public MSOpenClassContext(PCNode classNode, PCNode aspectNode) {
        this.classNode = classNode;
        this.aspectNode = aspectNode;
    }
    
    public PCNode getClassNode() {
        return classNode;
    }
    public PCNode getAspectNode() {
        return aspectNode;
    }
}
