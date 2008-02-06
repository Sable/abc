/* abc - The AspectBench Compiler
 * Copyright (C) 2008 Eric Bodden
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

package abc.da.types;

import polyglot.frontend.Source;
import polyglot.types.Context;
import polyglot.types.LazyClassInitializer;
import abc.aspectj.types.AspectType;
import abc.da.ast.DAAdviceDecl;
import abc.eaj.types.EAJTypeSystem_c;

/**
 * Type system for dependent advice extension. Creates {@link DAAspectType} nodes.
 * @author Eric Bodden
 */
public class DATypeSystem_c extends EAJTypeSystem_c
                             implements DATypeSystem
{
	
	public DATypeSystem_c() {
		//allow "dependent" flag in AJ context
		AJ_METHOD_FLAGS = AJ_METHOD_FLAGS.set(DAAdviceDecl.DEPENDENT);
		AJ_ADVICE_BODY_FLAGS = AJ_ADVICE_BODY_FLAGS.set(DAAdviceDecl.DEPENDENT);
	}

	@Override
	public Context createContext() {
		return new DAContext_c(this);
	}
	
	@Override
	public AspectType createAspectType(LazyClassInitializer init,
			Source fromSource, int perKind) {
		  return new DAAspectType_c(this, init, fromSource, perKind);
	}
}
