package org.aspectbench.tm.runtime.internal;

/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Pavel Avgustinov
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

import java.util.Set;

/**
 * An interface for the leaves of an IndexTree. Essentially, they're
 * sets of ITDBindings, and thus conform to the Set interface.
 * 
 * The main differences are that only weak references are kept to the
 * elements of the set, and that a cleanup() function is provided
 * to drop the set from an IndexTree if it's empty, and recurse up the
 * branch.
 * @author pavel
 */

public interface IndexTreeLeaf extends Set, WeakRefContainer {
	/**
	 * Check whether this set is empty; if so, recurse up the IndexTree
	 * dropping empty branches.
	 */
	public void cleanup();
}
