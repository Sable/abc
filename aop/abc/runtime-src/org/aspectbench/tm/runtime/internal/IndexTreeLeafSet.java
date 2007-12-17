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

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * A class implementing the sets of ITDBindings stored at the leaves
 * of an IndexTree. Comparison is by object identity, and only weak
 * references are kept to objects.
 * 
 * @author Pavel Avgustinov
 */

public class IndexTreeLeafSet extends AbstractSet implements IndexTreeLeaf {
	private static final Object dummy = new Object();
	private Map delegate = new java.util.IdentityHashMap(); //new WeakKeyCollectingIdentityHashMap();
	private IndexTreeMap.IndexTreeLevelMap parent;
	private Object parentKey;
	
	/**
	 * Construct a new IndexTreeLeafSet contained in the Map parent under the given
	 * key.
	 */
	public IndexTreeLeafSet(IndexTreeMap.IndexTreeLevelMap parent, Object key) {
		this.parent = parent;
		this.parentKey = key;
	}
	
	public boolean add(Object key) {
		return delegate.put(key, dummy) == null;
	}

	public void clear() {
		delegate.clear();
	}

	public boolean contains(Object key) {
		return delegate.containsKey(key);
	}

	public boolean remove(Object key) {
		return delegate.remove(key) != null;
	}

	public Iterator iterator() {
		return delegate.keySet().iterator();
	}

	public int size() {
		return delegate.size();
	}

	public void cleanup() {
		if(isEmpty()) {
			parent.remove(parentKey);
			parent.cleanupForKey(parentKey);
			parent = null;
		}
	}

	public void weakrefExpired(MyWeakRef ref) {
		remove(ref);
		cleanup();
	}

}
