/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
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

package abc.weaving.aspectinfo;

import polyglot.util.Position;

import java.util.*;

/** A <code>declare parents</code> .. <code>extends</code> declaration. 
 *  @author Aske Simon Christensen
 */
public class DeclareParentsExt extends DeclareParents {
    AbcClass parent;

    /** Create a <code>declare parents</code> extending a class.
     *  @param classes the classes that should extend the class. A collection of
     *                 {@link abc.weaving.aspectinfo.AbcClass} objects.
     *  @param parent the class to extend.
     */
    public DeclareParentsExt(ClassnamePattern pattern, Collection classes, AbcClass parent, Aspect aspct, Position pos) {
	super(pattern, classes, aspct, pos);
	this.parent = parent;
    }

    /** Get the extended class.
     *  @return the class to be extended.
     */
    public AbcClass getParent() {
	return parent;
    }

    public String toString() {
	return "declare parents: "+getClasses()+" extends "+parent.getJvmName()+";";
    }
}
