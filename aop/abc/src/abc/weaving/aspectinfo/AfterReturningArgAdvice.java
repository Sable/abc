/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
 * Copyright (C) 2004 Ganesh Sittampalam
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

import polyglot.util.Position;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.Bind;
import abc.weaving.residues.ContextValue;
import abc.weaving.residues.Residue;
import abc.weaving.residues.WeavingVar;

/** Advice specification for after returning advice with return variable binding. 
 *  @author Aske Simon Christensen
 *  @author Ganesh Sittampalam
 *  @author Eric Bodden
 */
public class AfterReturningArgAdvice extends AfterReturningAdvice {
    protected Formal formal;
	protected Var var;
	protected WeavingVar weavingVar;

    public AfterReturningArgAdvice(Formal formal, Position pos) {
	super(pos);
	this.formal = formal;
	this.var=new Var(formal.getName(),formal.getPosition());
    }

    public Formal getFormal() {
	return formal;
    }

    public String toString() {
	return "after returning arg";
    }

    public Residue matchesAt(WeavingEnv we,ShadowMatch sm,AbstractAdviceDecl ad) {
	if(super.matchesAt(we,sm,ad)==null) return null;
	ContextValue cv=sm.getReturningContextValue();
	weavingVar = we.getWeavingVar(var);
	return Bind.construct
	    (cv,we.getAbcType(var).getSootType(),weavingVar);
    }

}
