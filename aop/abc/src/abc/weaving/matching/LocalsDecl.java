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

package abc.weaving.matching;

import java.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.residues.*;

/** A weaving environment that handles locally scoped named pointcut variables
 *  @author Ganesh Sittampalam
 */

public class LocalsDecl implements WeavingEnv {
    private Hashtable/*<String,AbcType>*/ typeEnv;
    private Hashtable/*<String,LocalVar>*/ varEnv;
    private WeavingEnv child;

    public LocalsDecl(List/*<Formal>*/ formals,WeavingEnv child) {
	this.child=child;
	typeEnv=new Hashtable();
	varEnv=new Hashtable();
	Iterator it=formals.iterator();
	while(it.hasNext()) {
	    Formal f=(Formal) it.next();
	    typeEnv.put(f.getName(),f.getType());
	    varEnv.put(f.getName(),new LocalVar(f.getType().getSootType(),f.getName()));
	}
	
    }

    public WeavingVar getWeavingVar(Var v) {
	if(varEnv.containsKey(v.getName()))
	    return (LocalVar) varEnv.get(v.getName());
	else return child.getWeavingVar(v);
    }

    public AbcType getAbcType(Var v) {
	if(typeEnv.containsKey(v.getName()))
	    return (AbcType) typeEnv.get(v.getName());
	else return child.getAbcType(v);
    }
}
