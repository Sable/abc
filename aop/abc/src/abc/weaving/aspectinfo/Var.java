/* Abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2004 Damien Sereni
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

import java.util.Hashtable;
import polyglot.util.Position;
import abc.weaving.residues.*;

/** A pointcut variable. 
 *  @author Aske Simon Christensen
 *  @author Ganesh Sittampalam
 *  @author Damien Sereni
 */
public class Var extends Syntax {
    private String name;
    //    private int index;

    public Var(String name, Position pos) {
	super(pos);
	this.name=name;
    }

    //    public Var(String name, int index, Position pos) {
    //	super(pos);
    //	this.name = name;
    //	this.index = index;
    //}

    public String getName() {
	return name;
    }

    public String toString() {
	return name;
    }

    public Var rename(Hashtable/*<String,Var>*/ env) {
	if(env.containsKey(name)) return (Var) env.get(name);
	else return this;
    }
    
    /* canRenameTo: given another var and a list of existing substitutions,
     * can we rename this var to the other (the renaming is ok if there is no
     * previous binding, or the existing binding is the one we would have added) 
     * The new binding is added to the mapping */

	public boolean canRenameTo(Var other, Hashtable/*<Var,PointcutVarEntry>*/ renaming) {
		if (renaming.containsKey(this)) {
			GlobalCflowSetupFactory.PointcutVarEntry previous = 
				(GlobalCflowSetupFactory.PointcutVarEntry)renaming.get(this);
			return previous.equalsvar(other);
		} else {
			renaming.put(this, new GlobalCflowSetupFactory.PointcutVarEntry(other));
			return true;
		}
	}

    //public int getIndex() {
    //	return index;
    //}

    public boolean equals(Object o) {
	if (o instanceof Var) {
	    return ((Var)o).getName().equals(name);
	} else return false; 
    }

	public int hashCode() {
		return name.hashCode();
	}

}
