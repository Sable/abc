/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Oege de Moor
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

package abc.aspectj.types;

import polyglot.ext.jl.types.ParsedClassType_c;
import polyglot.frontend.Source;
import polyglot.types.LazyClassInitializer;
import polyglot.types.TypeSystem;

import abc.aspectj.visit.AccessorMethods;

/**
 * 
 * @author Oege de Moor
 *
 */
public class AspectType_c extends ParsedClassType_c implements AspectType {
	
	protected int perKind;
	
	protected AccessorMethods accessorMethods;

	public AspectType_c() {
		super();
		this.accessorMethods = new AccessorMethods();
	}

	
	public AspectType_c(
		TypeSystem ts,
		LazyClassInitializer init,
		Source fromSource, int perKind) {	
		super(ts, init, fromSource);
		this.perKind = perKind;
		this.accessorMethods = new AccessorMethods();
	}

	public int perKind() {
		if (perKind == PER_NONE)
		   	if (superType() instanceof AspectType)
			 	return ((AspectType)superType()).perKind();
			else 
				return AspectType.PER_SINGLETON;
		else 
			return perKind;
	}
	
	public boolean perObject() {
		int per = perKind();
		return (per == PER_THIS || per == PER_TARGET);
	}
	
	public AccessorMethods getAccessorMethods() {
	    return accessorMethods;
	}
}
