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

import java.util.*;

import polyglot.util.Position;
import soot.*;
import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** An argument pattern denoting any type. 
 *  @author Aske Simon Christensen
 *  @author Ganesh Sittampalam
 *  @author Damien Sereni
 */
public class ArgAny extends ArgPattern {
    public ArgAny(Position pos) {
	super(pos);
    }

    public Residue matchesAt(WeavingEnv we,ContextValue cv) {
	return AlwaysMatch.v;
    }

    public String toString() {
	return "*";
    }

    public Var substituteForPointcutFormal
	(Hashtable/*<String,Var>*/ renameEnv,
	 Hashtable/*<String,AbcType>*/ typeEnv,
	 Formal formal,
	 List/*<Formal>*/ newLocals,
	 List /*<CastPointcutVar>*/ newCasts,
	 Position pos) {

	String name=Pointcut.freshVar();
	Var v=new Var(name,pos);
	
	newLocals.add(new Formal(formal.getType(),name,pos));

	return v;
    }

    public void getFreeVars(Set/*<Var>*/ result) {}

	/* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.ArgPattern#equivalent(abc.weaving.aspectinfo.ArgPattern, java.util.Hashtable)
	 */
	public boolean equivalent(ArgPattern p, Hashtable renaming) {
		if (p instanceof ArgAny) {
			//FIXME ArgAny.equivalent(ArgType, ren) returns true; is this OK?
			System.out.println(p);
			return true;
		} else return false;
	}

}
