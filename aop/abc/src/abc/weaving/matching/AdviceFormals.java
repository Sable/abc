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

/** A weaving environment that can handle named pointcut variables 
 *  corresponding to formal arguments to a piece of advice
 *  @author Ganesh Sittampalam
 */

public class AdviceFormals implements WeavingEnv {
    private AdviceDecl ad;

    public AdviceFormals(AdviceDecl ad) {
	this.ad=ad;
    }

    private Hashtable adviceformals=new Hashtable();
    public WeavingVar getWeavingVar(Var v) {
	if(adviceformals.containsKey(v.getName())) 
	    return (AdviceFormal) adviceformals.get(v.getName());
	AdviceFormal adviceformal=new AdviceFormal
	    (ad.getFormalIndex(v.getName()),
	     ad.getFormalType(v.getName()).getSootType());
	adviceformals.put(v.getName(),adviceformal);
	return adviceformal;
    }

    public AbcType getAbcType(Var v) {
	return ad.getFormalType(v.getName());
    }
}
