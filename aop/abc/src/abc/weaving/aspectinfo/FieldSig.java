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

import soot.*;

/** A field signature. 
 *  @author Aske Simon Christensen
 */
public class FieldSig extends Sig {
    private int mod;
    private AbcClass cl;
    private AbcType type;
    private String name;
    private SootFieldRef sfr=null;

    public FieldSig(int mod, AbcClass cl, AbcType type, String name, Position pos) {
	super(pos);
	this.mod = mod;
	this.cl = cl;
	this.type = type;
	this.name = name;
    }

    public int getModifiers() {
	return mod;
    }

    public AbcClass getDeclaringClass() {
	return cl;
    }

    public AbcType getType() {
	return type;
    }

    public String getName() {
	return name;
    }

    public ClassMember getSootMember() {
	return getSootField();
    }

    public SootFieldRef getSootFieldRef() {
	if (sfr == null) {
	    SootClass sc = cl.getSootClass();
	    soot.Type st = type.getSootType();
	    sfr = Scene.v().makeFieldRef(sc,name,st,java.lang.reflect.Modifier.isStatic(mod));
	}
	return sfr;
    }

    public SootField getSootField() {
	return getSootFieldRef().resolve();
    }

    public String toString() {
	return Modifier.toString(mod)+" "+type+" "+cl+"."+name;
    }

    /** Checks whether the two field signatures refer to the same field. */
    public boolean equals(Object other) {
	if (!(other instanceof FieldSig)) return false;
	FieldSig os = (FieldSig)other;
	return
	    cl.equals(os.cl) &&
	    type.equals(os.type) &&
	    name.equals(os.name);
    }
    
	public int hashCode() {
	  return cl.hashCode()+type.hashCode()+name.hashCode();
	}
}
