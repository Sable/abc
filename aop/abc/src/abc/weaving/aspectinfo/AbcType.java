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

import polyglot.types.ClassType;
import polyglot.util.InternalCompilerError;

import soot.*;

/** A Java type handled by the ABC compiler. 
 *  @author Aske Simon Christensen
 */
public class AbcType {
    private soot.Type st;
    private polyglot.types.Type pt;

    AbcType(soot.Type st) {
	if (st == null) throw new RuntimeException("AbcType with null Soot type");
	this.st = st;
    }

    AbcType(polyglot.types.Type pt) {
	if (pt == null) throw new RuntimeException("AbcType with null Polyglot type");
	this.pt = pt;
    }

    public soot.Type getSootType() {
	if (st == null) {
	    //System.out.println("Getting soot type for "+pt);
	    if (pt instanceof ClassType) {
		try {
		    st = AbcFactory.classTypeToSootClass((ClassType)pt).getType();
		} catch (NullPointerException e) {
		    throw new InternalCompilerError("Soot type of AbcType "+pt+" not ready yet");
		}
	    } else {
		st = soot.javaToJimple.Util.getSootType(pt);
	    }
	}
	return st;
    }

    public String toString() {
	return getSootType().toString();
    }

    public boolean equals(Object other) {
	if (!(other instanceof AbcType)) return false;
	AbcType ot = (AbcType)other;
	if (pt != null && ot.pt != null)
	    return pt.equals(ot.pt);
	return getSootType().equals(ot.getSootType());
    }

    public int hashCode() {
	// No nontrivial valid hashcode possible
	return 0;
    }
}
