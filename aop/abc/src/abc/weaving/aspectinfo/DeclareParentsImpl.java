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

/** A <code>declare parents</code> .. <code>implements</code> declaration. 
 *  @author Aske Simon Christensen
 */
public class DeclareParentsImpl extends DeclareParents {
    List/*<AbcClass>*/ interfaces;

    /** Create a <code>declare parents</code> implementing a list of interfaces.
     *  @param classes the classes that should implement the interfaces. A collection of
     *                 {@link abc.weaving.aspectinfo.AbcClass} objects.
     *  @param interfaces a list of {@link abc.weaving.aspectinfo.AbcClass} objects giving the
     *         interfaces to be implemented.
     */
    public DeclareParentsImpl(ClassnamePattern pattern, Collection classes, List interfaces, Aspect aspct, Position pos) {
	super(pattern, classes, aspct, pos);
	this.interfaces = interfaces;
    }

    /** Get the list of implemented interfaces.
     *  @return a list of {@link abc.weaving.aspectinfo.AbcClass} objects.
     */
    public List/*<AbcClass>*/ getInterfaces() {
	return interfaces;
    }

    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("declare parents: ");
	sb.append(getClasses());
	sb.append(" implements ");
	Iterator ii = getInterfaces().iterator();
	while (ii.hasNext()) {
	    AbcClass i = (AbcClass)ii.next();
	    sb.append(i.getJvmName());
	    if (ii.hasNext()) {
		sb.append(", ");
	    }
	}
	sb.append(";");
	return sb.toString();
    }
}
