/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
 * Copyright (C) 2004 Ganesh Sittampalam
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

import soot.*;
import soot.jimple.*;

import java.util.*;

import polyglot.util.Position;

import abc.weaving.matching.*;
import abc.weaving.residues.Residue;

/** A pointcut designator representing a set of joinpoint shadows
 *  at which the pointcut will match.
 *  @author Aske Simon Christensen
 *  @author Ganesh Sittampalam
 */
public abstract class ShadowPointcut extends Pointcut {
    public final Residue matchesAt(WeavingEnv env,
				   SootClass cls,
				   SootMethod method,
				   ShadowMatch sm) {
	return matchesAt(sm);
    }

    public ShadowPointcut(Position pos) {
	super(pos);
    }

    /** Shadow pointcuts just need to know the ShadowMatch */
    protected abstract Residue matchesAt(ShadowMatch sm);

    protected Pointcut inline(Hashtable renameEnv,
			      Hashtable typeEnv,
			      Aspect context) {
	return this;
    }

    public void registerSetupAdvice
	(Aspect aspect,Hashtable/*<String,AbcType>*/ typeMap) {}
    public void getFreeVars(Set/*<Var>*/ result) {}
}
