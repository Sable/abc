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
 * As this is part of an IndexTree data structure, the values will all
 * be boxed in MaybeWeakRefs (the tm update code needs to control
 * strength of references). Since these are canonical, a simple
 * IdentityHashMap suffices as a delegate object.
 * 
 * @author Pavel Avgustinov
 */

public class IndexTreeLeafSet extends AbstractSet implements IndexTreeLeaf {
	private static final Object dummy = new Object();
	private IdentityHashMap delegate = new IdentityHashMap();
	private IndexTreeMap.IndexTreeLevelMap parent;
	private Object parentKey;
	
	/**
	 * Special iterator ranging over a collection of MaybeWeakRefs that
	 * unboxes them before removing.
	 * 
	 * Note that a special contract applies: As objects may disappear from
	 * the map, hasNext() can only tell when there definitely isn't a next
	 * element; if it returns true, then next() will still return null if
	 * all remaining elements have been dropped in the meantime.
	 * @author pavel
	 *
	 */
	static class UnboxingIterator implements Iterator {
		Iterator delegate;
		UnboxingIterator(Iterator d) {
			delegate = d;
		}
		
		public boolean hasNext() {
			return delegate.hasNext();
		}
		
		public Object next() {
			Object result;
			do {
				MaybeWeakRef ref = ((MaybeWeakRef)delegate.next());
				if(ref == null) return null;
				result = ref.get();
			} while(result == null);
			return result;
		}
		
		public void remove() {
			throw new UnsupportedOperationException("Shouldn't remove values from IndexTree while iterating");
		}
		
	}
	
	/**
	 * Construct a new IndexTreeLeafSet contained in the Map parent under the given
	 * key.
	 */
	public IndexTreeLeafSet(IndexTreeMap.IndexTreeLevelMap parent, Object key) {
		this.parent = parent;
		this.parentKey = key;
	}
	
	public boolean add(Object key) {
		((MaybeWeakRef)key).addContainer(this);
		return delegate.put(key, dummy) == null;
	}

	public void clear() {
		delegate.clear();
	}

	public boolean contains(Object key) {
		return delegate.containsKey(key);
	}

	public boolean remove(Object key) {
		((MaybeWeakRef)key).removeContainer(this);
		return delegate.remove(key) != null;
	}

	public Iterator iterator() {
		return new UnboxingIterator(delegate.keySet().iterator());
	}

	public int size() {
		return delegate.size();
	}

	public void cleanup() {
		if(isEmpty() && parent != null) {
			parent.remove(parentKey);
			parent.cleanupForKey(parentKey);
			parent = null;
		}
	}

	public void weakrefExpired(MyWeakRef ref) {
		// No need to disassociate from container, as the whole containers list
		// is about to be dropped...
		// ref.removeContainer(this);
		delegate.safeRemove(ref);
		cleanup();
	}

}
