/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
 * Copyright (C) 2004 Damien Sereni
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
import polyglot.util.InternalCompilerError;

import soot.*;

import java.util.*;

/** A formal parameter to a method or constructor. 
 *  @author Aske Simon Christensen
 *  @author Damien Sereni
 */
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
    
    /** Checks whether there is a binding with source var having the
     *  same name as this formal in a renaming
     * 
     * @param renaming The Renaming(Var->VarBox) 
     */
    public boolean isInRenamingAsSource
				(Hashtable/*<Var, PointcutVarEntry>*/ renaming) {
    	// NOTE that Var.equals() compares by name only, so can
    	// create a new Var object and remove just that
    	Var v = new Var(name, getPosition());
    	if (renaming.containsKey(v)) {
    		VarBox ve = 
				(VarBox) renaming.get(v);
    		return (ve.hasVar());
    	} else return false;
    }
    
    public void removeFromRenamingAsSource
    			(Hashtable/*<Var, PointcutVarEntry>*/ renaming) {
    	Var v = new Var(name, getPosition());
    	if (renaming.containsKey(v)) {
    		renaming.remove(v);
    	}
    }
    
}
