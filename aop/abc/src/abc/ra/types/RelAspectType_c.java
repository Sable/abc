/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Eric Bodden
 * Copyright (C) 2007 Reehan Shaikh
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
package abc.ra.types;

import java.util.List;

import polyglot.frontend.Source;
import polyglot.types.LazyClassInitializer;

/**
 * Type for a potentially relational aspect.
 *
 * @author Eric Bodden
 */
public class RelAspectType_c extends abc.aspectj.types.AspectType_c implements RelAspectType {
	
	protected boolean isRelational;
	
	protected List relationalAspectFormals;

	public RelAspectType_c(RATypeSystem_c typeSystem_c, LazyClassInitializer init,
			Source fromSource, int perKind) {
		super(typeSystem_c,init,fromSource,perKind);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean relational() {
		return isRelational;
	}

	/**
	 * {@inheritDoc}
	 */
	public void relational(boolean isRelational) {
		this.isRelational = isRelational;
	}

	/**
	 * {@inheritDoc}
	 */
	public List relationalAspectFormals() {
		return relationalAspectFormals;
	}

	/**
	 * {@inheritDoc}
	 */
	public void relationalAspectFormals(List formals) {
		relationalAspectFormals = formals;
	}

}
