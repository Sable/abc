/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
 * Copyright (C) 2004 Ganesh Sittampalam
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

	public boolean unify(Var other, Unification unification) {
		// First, check that the types are equal - only want to unify variables if
		// the types are equals
		// TODO would it be ok to require one to be a supertype of or equal to the other
		// TODO What about the boxing behaviour for primitive types?
		
		if (!unification.getType1(this.getName()).equals(unification.getType2(other.getName())))
		{ 	if (abc.main.Debug.v().debugPointcutUnification)
				System.out.println("Rejected unification between "+this+" and "+other+" - incompatible types");
			return false;
		}
		
		//   If neither this nor other is a target in ren1/ren2(resp) can add the
		// bindings this->this, this->other (resp) and unification succeeds
		//   If this is a target but other is not, need to add the binding for
		// other, and vice-versa if other is a target
		//   If both are targets, need to check that they are targets of the same
		// var. If they are, unification suceeds, otherwise fails
		
		if (unification.containsVarValue1(this)) {
			Var v1 = unification.getByVarValue1(this);
			if (unification.unifyWithFirst()) {
				// SANITY CHECK: if unifyWithFirst(), then the first renaming should
				// always be a partial id map
				if (!v1.equals(this))
					throw new RuntimeException("Unfication error: restricted unification failed (Var):" +						" renaming1 should be id map but we have "+v1+"->"+this);
			}
			if (unification.containsVarValue2(other)) {
				Var v2 = unification.getByVarValue2(other);
				// Both this and other are targets
				if (v1.equals(v2)) {
					// The two vars are already unified
					// NOTE In order to always return THIS when doing restricted unification,
					// need to check for the case v1.equals(this) here - note that this can occur
					// b/c a previous var with the same name will have been used to create the
					// binding. No loss of generality as whenever v1 is used in the hashtable,
					// THIS will do just as well as v1.equals(THIS)
					if (v1.equals(this))
						unification.setVar(this);
					else
						unification.setVar(v1);
					return true;
				} else {
					// The two vars are not mapped to the same var
					return false;
				}
			} else {
				// this is a target but other is not
				unification.putVar2(v1, other);
				// See above: special case if v1.equals(this)
				if (v1.equals(this))
					unification.setVar(this);
				else
					unification.setVar(v1);
				return true;
			}
		} else
		if (unification.containsVarValue2(other)) {
			Var v2 = unification.getByVarValue2(other);
			// other is a target but this is not
			
			// If we are doing restricted unification, there has been a problem - 
			// we should never bind a variable in 2 without binding the corresponding
			// var in 1 as well
			if (unification.unifyWithFirst())
				throw new RuntimeException("Unification error: restricted unification failed (Var):"+
				 " var1="+this+" has no binding but var2="+other+" is the target of "+v2);
			
			unification.putVar1(v2, this);
			unification.setVar(v2);
			return true;
		} else {
			// Neither is a target
			// Can we use THIS or OTHER to map them from?
			// o/w will have to create a new var
			Var newkey;
			if (!(unification.containsKey1(this) || unification.containsKey2(this)))
				newkey = this;
		    else {
		    	// IF we are doing restricted unification, then unification.containsKey2(this)
		    	// should ALWAYS imply unification.containsKey1(this). But in restricted unification
		    	// the mapping ren1 should only ever be a partial id map, so why was this->this not
		    	// in it?
		    	// For easier debug try to produce a precise message
		    	if (unification.unifyWithFirst()) {
		    		 if (!(unification.containsKey1(this)))
		    		 	throw new RuntimeException("Unification error: restricted unification failed (Var): " +		    		 		"ren2 has a binding for "+this+"->"+unification.get2(this)+" but " +		    		 			"ren1 does not have a binding for "+this);
		    		 else
						throw new RuntimeException("Unification error: restricted unification failed (Var): " +
							"ren1 is not an id map, it has a binding "+this+"->"+
							unification.get1(this));
		    	}
		    	// End of sanity check
		    	
		    	if (!(unification.containsKey1(other) || unification.containsKey2(other)))
		    		newkey = other;
		    	else
		    		newkey = makeNewPcVar(unification);
		    }
				
			unification.putVar1(newkey, this);
			unification.putVar2(newkey, other);
			unification.setVar(newkey);
			return true;
		}
		
	}
	
	private Var makeNewPcVar(Unification unification) {
		int i = 0;
		String s = "pcvar";
		while (unification.containsKeyAsString1(s)
				|| unification.containsKeyAsString2(s)) {
			i++;
			s = "pcvar$"+i;
		}
		return new Var(s, getPosition());
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
