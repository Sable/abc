/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
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

import abc.weaving.residues.*;

/** A pointcut designator representing every join point
 *  @author Ganesh Sittampalam
 */

public class FullPointcut extends LexicalPointcut {

    public FullPointcut(Position pos) {
	super(pos);
    }

    protected Residue matchesAt(SootClass cls,SootMethod method) {
	return AlwaysMatch.v();
    }
    
    public String toString() {
	return "full";
    }

	/* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.Pointcut#unify(abc.weaving.aspectinfo.Pointcut, java.util.Hashtable, java.util.Hashtable, abc.weaving.aspectinfo.Pointcut)
	 */
	public boolean unify(Pointcut otherpc, Unification unification) {

		if (otherpc.getClass() == this.getClass()) {
			unification.setPointcut(this);
			return true;
		} else // Do the right thing if otherpc was a local vars pc
			return LocalPointcutVars.unifyLocals(this,otherpc,unification);

	}
}
