/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2004 Damien Sereni
 * Copyright (C) 2006 Eric Bodden
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
import java.util.List;
import java.util.Set;

import polyglot.util.Position;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.ContextValue;
import abc.weaving.residues.Residue;

/** A pattern for a single argument. 
 *  @author Aske Simon Christensen
 *  @author Ganesh Sittampalam
 *  @author Damien Sereni
 *  @author Eric Bodden
 */
public abstract class ArgPattern extends Syntax {
    public ArgPattern(Position pos) {
	super(pos);
    }

    /** For use when being used in an args pattern */
    public abstract Residue matchesAt(WeavingEnv we,ContextValue cv);

    /** For use when this is being used as a pointcut formal */
    public abstract Var substituteForPointcutFormal
	(Hashtable/*<String,Var>*/ renameEnv,
	 Hashtable/*<String,AbcType>*/ typeEnv,
	 Formal formal,
	 List/*<Formal>*/ newLocals,
	 List /*<CastPointcutVar>*/ newCasts,
	 Position pos);

    public abstract void getFreeVars(Set/*<String>*/ result);

	/** Attempts to unify two pointcuts, creating another pointcut that has enough variables
	 *  to encompass both if possible. Variables are only unified if they have the same type,
	 *  as stored in the unification typemaps. If unification.unifyWithFirst(), then restricted
	 *  unification is attempted, which succeeds only if both pointcuts can be unified with result
	 *  the first pointcut (ie the first pointcut has no less free variables than the second).
	 * 
	 * @param other The other pointcut to unify with
	 * @param unification The unification. This should be initialized (determining
	 * whether we attempt proper or restricted unification) and the typemaps should
	 * be set.
	 * @return True iff the unification was successful. In this case, unification contains
	 * the resulting pointcut and the substitutions taking it to THIS and OTHER. Otherwise,
	 * unification is left in any old state.
	 */

	public abstract boolean unify(ArgPattern other, Unification unification);

}
