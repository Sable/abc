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

/** A <code>declare parents</code> declaration
 *  @author Aske Simon Christensen
 */
public abstract class DeclareParents extends InAspect {
    private ClassnamePattern pattern;
    private Collection/*<AbcClass>*/ classes;

    /** Make a <code>declare parents</code> declaration.
     *  @param classes a collection of {@link abc.weaving.aspectinfo.AbcClass} objects.
     */
    public DeclareParents(ClassnamePattern pattern, Collection classes, Aspect aspct, Position pos) {
	super(aspct, pos);
	this.pattern = pattern;
	this.classes = classes;
    }

    public ClassnamePattern getPattern() {
	return pattern;
    }

    /** Get the classes whose parents are being declared.
     *  @return a collection of {@link abc.weaving.aspectinfo.AbcClass} objects.
     */
    public Collection getClasses() {
	return classes;
    }
}
