/* Abc - The AspectBench Compiler
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
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
import polyglot.util.InternalCompilerError;

import soot.*;

import java.util.*;

/** A formal parameter to a method or constructor. */
public class Formal extends Syntax {
    private AbcType type;
    private String name;

    public Formal(AbcType type, String name, Position pos) {
	super(pos);
	this.type = type;
	this.name = name;
	if(name==null) 
	    throw new InternalCompilerError("Constructing a formal with a null name");
	if(type==null) 
	    throw new InternalCompilerError("Constructing a formal with a null type");
    }

    public AbcType getType() {
	return type;
    }

    public String getName() {
	return name;
    }

    public String toString() {
	return type+" "+name;
    }

    public boolean equals(Object other) {
	if (!(other instanceof Formal)) return false;
	Formal of = (Formal)other;
	return type.equals(of.type);
    }

    public int hashCode() {
	return type.hashCode();
    }
    
    public boolean canRenameTo(Formal f, Hashtable/*<Var, Var>*/ renaming) {    	
    	if (type.equals(f.getType())) {
    		// NOTE: the renaming maps Vars to Vars as this is what we need later
    		// Formals are NEVER added to the renaming, we only check that there already
    		// is a corresponding entry
    		// Var objects are compared by name, so it is OK to create two new Vars for the
    		// test
    		// TODO Check that it is OK that Formal.canRenameTo does not add bindings
    		
    		Var thisv = new Var(name, getPosition());
    		Var otherv = new Var(f.getName(), f.getPosition());
    		
    		if (renaming.containsKey(thisv)) {
    			Var previous = (Var)renaming.get(thisv);
    			if (previous.getName().equals(f.getName())) {
    				return true;
    			} else return false; // Existing match, wrong name 
    		} else return false;     // No match
    		} else return false;     // Wrong type
    }
}
