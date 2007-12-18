package org.aspectbench.tm.runtime.internal;

import java.util.Iterator;

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

/**
 * Interface for a fully indexed tree of tracematch constraints
 */
public interface IndexTree {
	/**
	 * Insert a particular ITDBinding object into the tree for the
	 * given keys. TODO: How do we handle primitive bindings?
	 * 
	 * As the update code needs control over strength of stored
	 * references, values in the IndexTree are MaybeWeakRefs, and
	 * we trust the update code to keep anything alive that needs
	 * to be alive.
	 */
	public void insert(Object[] keys, MaybeWeakRef value);
	
	/**
	 * Get an iterator over all ITDBinding objects for the given
	 * set of keys, or null if none.
	 * 
	 * Note that the iterator unboxes the MaybeWeakRefs, so calling
	 * next() either returns an actual value or null.
	 */
	public Iterator get(Object[] keys);
}
