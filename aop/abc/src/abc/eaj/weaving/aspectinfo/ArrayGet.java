/* abc - The AspectBench Compiler
 * Copyright (C) 2008 Pavel Avgustinov
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

package abc.eaj.weaving.aspectinfo;

import soot.*;

import polyglot.util.Position;

import abc.weaving.aspectinfo.*;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.residues.*;

import abc.eaj.weaving.matching.*;

/**
 * @author Pavel Avgustinov
 *
 */

public class ArrayGet extends ShadowPointcut {
	public ArrayGet(Position pos) {
		super(pos);
	}
	
	protected Residue matchesAt(ShadowMatch sm) {
		if(sm instanceof ArrayGetShadowMatch)
			return AlwaysMatch.v();
		else 
			return NeverMatch.v();
	}

	public String toString() {
		return "arrayget()";
	}
	
	public boolean unify(Pointcut other, Unification uni) {
        if (other.getClass() == this.getClass())
            return true;
        else // Do the right thing if otherpc was a local vars pc
            return LocalPointcutVars.unifyLocals(this,other, uni);
	}

}
