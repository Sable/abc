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
import soot.*;
import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** An argument pattern denoting a specific type. 
 *  @author Aske Simon Christensen
 *  @author Ganesh Sittampalam
 *  @author Damien Sereni
 */
public class ArgType extends ArgAny {
    private AbcType type;

    public String toString() {
	return type.toString();
    }

    public ArgType(AbcType type, Position pos) {
	super(pos);
	this.type = type;
    }

    public AbcType getType() {
	return type;
    }

    public Residue matchesAt(WeavingEnv we,ContextValue cv) {
	return CheckType.construct(cv,type.getSootType());
    }

	/* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.ArgPattern#unify(abc.weaving.aspectinfo.ArgPattern, abc.weaving.aspectinfo.Unification)
	 */
	public boolean unify(ArgPattern other, Unification unification) {
		if (other.getClass() == this.getClass()) {
			if (type.equals(((ArgType)other).getType())) {
				unification.setArgPattern(this);
				return true;
			} else if ((other.getClass() == ArgVar.class)
						&& (!unification.unifyWithFirst())) {
				// If the other pc is a ArgVar with the same type as this, can unify
				// BUT Only if not restricted to returning THIS
				if (abc.main.Debug.v().debugPointcutUnification)
					System.out.println("Trying to unify an ArgType "+this+" with an ArgVar: "+other);
				ArgVar otherav = (ArgVar)other;
				if (this.getType().equals(unification.getType2(otherav.getVar().getName()))) {
					if (abc.main.Debug.v().debugPointcutUnification)
						System.out.println("Succeeded!");
					unification.setArgPattern(otherav);
					unification.put1(otherav.getVar(), new VarBox());
					return true;
				} else return false;
			} else return false;
		} else return false;
	}
}
