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

import abc.aspectj.visit.PCStructure;

import soot.Scene;
import soot.SootClass;

/** A Java class handled by the ABC compiler. 
 *  @author Aske Simon Christensen
 */
public class AbcClass {
    private String java_name;
    private String jvm_name;
    private ClassType polyglot_type;
    private SootClass sc;

    AbcClass(ClassType polyglot_type, String java_name) {
	this.polyglot_type = polyglot_type;
	this.java_name = java_name;
	polyglot_type.toString();
    }

    AbcClass(ClassType polyglot_type) {
	this.polyglot_type = polyglot_type;
	polyglot_type.toString();
    }

    AbcClass(SootClass sc) {
	this.sc = sc;
	this.jvm_name = sc.getName();
    }

    public ClassType getPolyglotType() {
	return polyglot_type;
    }

    public String getJavaName() {
	return java_name;
    }

    public String getJvmName() {
	if (jvm_name == null) {
	    //System.err.println(((polyglot.types.ClassType)polyglot_type).fullName());
	    try {
		jvm_name = AbcFactory.classTypeToSootClass(polyglot_type).toString();
	    } catch (NullPointerException e) {
		throw new InternalCompilerError("SootClass of AbcClass "+polyglot_type+" not ready yet");
	    }
	}
	return jvm_name;
    }

    public SootClass getSootClass() {
	if (sc == null) {
	    sc = Scene.v().getSootClass(getJvmName());
	}
	return sc;
    }

    public String toString() {
	return getJvmName();
    }

    public boolean equals(Object other) {
	if (!(other instanceof AbcClass)) return false;
	AbcClass oc = (AbcClass)other;
	if (polyglot_type != null && oc.polyglot_type != null)
	    return polyglot_type.equals(oc.polyglot_type);
	return getSootClass().equals(oc.getSootClass());
    }

    public int hashCode() {
	// No nontrivial valid hashcode possible
	return 0;
    }
}
