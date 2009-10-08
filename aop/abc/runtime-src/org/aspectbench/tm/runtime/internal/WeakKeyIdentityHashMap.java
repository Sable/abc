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

import org.aspectbench.tm.runtime.internal.IdentityHashMap.HashEntry;

/**
 * Part of the Indexing Data Structure implementation. This is a HashMap-like
 * data structure that uses key identity as the basis for comparison. It isn't
 * quite conformant to the java.util.Map interface, since its hasNext() method
 * is (by design) unreliable. Instead, next() should be called and the return
 * value checked for non-nullness.
 *
 * This class uses weak references for its keys, but does *not* drop a mapping
 * after a key expires -- it is still iterable.
 * 
 * Only the methods of the Map interface that are required by the IDS are
 * implemented, all others just throw an exception.
 * 
 * @author Pavel Avgustinov
 */

public class WeakKeyIdentityHashMap extends IdentityHashMap {

	/**
	 * HashEntrys for this kind of map return the object if getKey() is called
	 * and the object is alive.
	 */
	public class HashEntry extends IdentityHashMap.HashEntry {
		private PersistentWeakRef key;
		
		protected HashEntry(HashEntry next, PersistentWeakRef key, int hashCode, Object value) {
			super(next, hashCode, value);
		}

		public Object getKey() {
			return key.get();
		}
		
		protected int getKeyHash() {
			return key.hashCode();
		}
	}
	
	public WeakKeyIdentityHashMap() {
		super();
	}

	public WeakKeyIdentityHashMap(int initialCapacity, float loadFactor, int threshold) {
		super(initialCapacity, loadFactor, threshold);
	}

	public WeakKeyIdentityHashMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	public WeakKeyIdentityHashMap(int initialCapacity) {
		super(initialCapacity);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Object get(Object key) {
	    key = PersistentWeakRef.getWeakRef(key);
		return super.get(key);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object put(Object key, Object value) {
		key = PersistentWeakRef.getWeakRef(key);
		return super.put(key, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object remove(Object key) {
		return super.remove(key);
	}
	
}
