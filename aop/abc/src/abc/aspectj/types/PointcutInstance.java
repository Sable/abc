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

import java.util.List;
import java.util.Set;

import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;


public interface PointcutInstance {
	String toString();

	void setDynamic(boolean dynamic);

	boolean isDynamic();

	void setRefersTo(Set x);

	Set getRefersTo();

	Set transRefs();

	boolean cyclic();

	boolean checkAbstract(AJContext c);

	/** deprecated */
	boolean transAbstract();

	boolean checkDynamic(AJContext c);

	/** deprecated */
	boolean transDynamic();

	String signature();

	String designator();

	boolean isSameMethodImpl(MethodInstance mj);

	List implementedImpl(ReferenceType rt);

	boolean canOverrideImpl(MethodInstance mj, boolean quiet)
			throws SemanticException;
}
