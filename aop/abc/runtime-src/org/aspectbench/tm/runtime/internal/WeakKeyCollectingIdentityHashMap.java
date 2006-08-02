/* abc - The AspectBench Compiler
 * Copyright (C) 2006 Pavel Avgustinov
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

package org.aspectbench.tm.runtime.internal;

/**
 * Part of the Indexing Data Structure implementation. This is a HashMap-like
 * data structure that uses key identity as the basis for comparison. It isn't
 * quite conformant to the java.util.Map interface, since its hasNext() method
 * is (by design) unreliable. Instead, next() should be called and the return
 * value checked for non-nullness.
 * 
 * This class uses weak references to the keys, not preventing garbage collection.
 * In fact, when a key expires, the corresponding mapping is dropped.
 * 
 * Only the methods of the Map interface that are required by the IDS are
 * implemented, all others just throw an exception.
 * 
 * @author Pavel Avgustinov
 */

import java.util.Set;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.Reference;

public class WeakKeyCollectingIdentityHashMap extends IdentityHashMap {
	ReferenceQueue queue = new ReferenceQueue();

	/**
	 * This HashEntry uses weak references to the keys, and registers a queue which is
	 * polled before any significant operation --- the GC puts expired weakrefs into
	 * that queue, and they are removed from the map.
	 * 
	 * @author Pavel Avgustinov
	 */
	class WeakKeyHashEntry extends IdentityHashMap.HashEntry {
		private MyWeakRef key;
		
		public WeakKeyHashEntry(HashEntry next, Object key, int hashCode, Object value, ReferenceQueue q) {
			super(next, hashCode, value);
			this.key = new MyWeakRef(key, q);
		}

		protected Object getKey() {
			return key.get();
		}
		
	}
	
	public WeakKeyCollectingIdentityHashMap() {
		super();
	}

	public WeakKeyCollectingIdentityHashMap(int initialCapacity, float loadFactor, int threshold) {
		super(initialCapacity, loadFactor, threshold);
	}

	public WeakKeyCollectingIdentityHashMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	public WeakKeyCollectingIdentityHashMap(int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * Perform the cleanup, i.e. drop all expired key/value pairs.
	 */
	public void cleanup() {
		Reference ref = queue.poll();
		while(ref != null) {
			safeRemove(ref.get());
			ref = queue.poll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void clear() {
		// drain queue
		while(queue.poll() != null);
		super.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object get(Object key) {
		cleanup();
		return super.get(key);
	}

	/**
	 * {@inheritDoc}
	 */
	HashEntry createHashEntry(HashEntry next, Object key, int hash, Object value) {
		return new WeakKeyHashEntry(next, key, hash, value, queue);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isEmpty() {
		cleanup();
		return super.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	protected KeyIterator keyIterator() {
		cleanup();
		return super.keyIterator();
	}

	/**
	 * {@inheritDoc}
	 */
	public Set keySet() {
		cleanup();
		return super.keySet();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object put(Object key, Object value) {
		cleanup();
		return super.put(key, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object remove(Object key) {
		cleanup();
		return super.remove(key);
	}

	/**
	 * {@inheritDoc}
	 */
	public int size() {
		cleanup();
		return super.size();
	}

}
