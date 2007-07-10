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
 * Created on May 15, 2005
 *
 */
package abc.om.visit;

import polyglot.util.Position;

/**
 * Internal representation of module hierarchy.
 * 
 * @author Neil Ongkingco
 *  
 */
public abstract class ModuleNode {
    /* enum ModuleType */
    public static final int TYPE_NULL = 0;

    public static final int TYPE_MODULE = 1;

    public static final int TYPE_ASPECT = 2;

    public static final int TYPE_CLASS = 3;

    protected String name;

    protected ModuleNode parent;
    
    protected Position pos;

    public String name() {
        return name;
    }
    
    public String toString() {
        return name;
    }

    public abstract boolean isAspect();

    public abstract boolean isModule();

    public abstract boolean isClass();

    public abstract int type();

    public void setParent(ModuleNode n) {
        this.parent = n;
    }

    public ModuleNode getParent() {
        return this.parent;
    }

    public Position position() {
        return pos;
    }
    
    public void setPosition(Position pos) {
        this.pos = pos;
    }
}
