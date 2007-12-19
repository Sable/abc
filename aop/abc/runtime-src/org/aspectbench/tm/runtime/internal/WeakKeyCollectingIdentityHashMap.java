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

import java.util.Iterator;
import java.util.Set;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.Reference;

public class WeakKeyCollectingIdentityHashMap extends IdentityHashMap {
	private static boolean instancesExist = false;
	
	/**
	 * Specialised MyWeakRef class that keeps a reference to the map it was created for.
	 * Intended for easier map cleanups -- it can be registered in a centralised
	 * reference queue, and when it expires the associated map can be retreived from it.
	 */
	class KeyWeakRef extends MyWeakRef {
		protected WeakKeyCollectingIdentityHashMap theMap = WeakKeyCollectingIdentityHashMap.this; 
		
		public KeyWeakRef(Object o) {
			super(o, expiredQueue);
		}
		
		public void cleanup() {
			theMap.safeRemove(this);
		}
	}
	
	/**
	 * This HashEntry uses weak references to the keys, and registers a queue which is
	 * polled before any significant operation --- the GC puts expired weakrefs into
	 * that queue, and they are removed from the map.
	 * 
	 * @author Pavel Avgustinov
	 */
	class WeakKeyHashEntry extends IdentityHashMap.HashEntry {
		private KeyWeakRef key;
		
		public WeakKeyHashEntry(HashEntry next, Object key, int hashCode, Object value) {
			super(next, hashCode, value);
			this.key = new KeyWeakRef(key);
		}

		public Object getKey() {
			return key.get();
		}
		
		protected Reference getRawKey() {
			return key;
		}
		
		protected int getKeyHash() {
			return key.hashCode();
		}

	}
	
	public WeakKeyCollectingIdentityHashMap() {
		super();
		instancesExist = true;
	}

	public WeakKeyCollectingIdentityHashMap(int initialCapacity, float loadFactor, int threshold) {
		super(initialCapacity, loadFactor, threshold);
		instancesExist = true;
	}

	public WeakKeyCollectingIdentityHashMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
		instancesExist = true;
	}

	public WeakKeyCollectingIdentityHashMap(int initialCapacity) {
		super(initialCapacity);
		instancesExist = true;
	}

	/**
	 * {@inheritDoc}
	 */
	HashEntry createHashEntry(HashEntry next, Object key, int hash, Object value) {
		return new WeakKeyHashEntry(next, key, hash, value);
	}

	/**
	 * Removes the key/value pair corresponding to the hidden key in a "safe" way.
	 * 
	 * What this means is that it doesn't update the modCount. It's intended for
	 * this method to be called when automatically purging values from the map.
	 * In this way, we allow removal of values while iterating over the keyset,
	 * but still catch "non-safe", i.e. user-initiated, removals.
	 * 
	 * Also, the key is assomed to be a WeakReference, and is NOT unboxed.
	 */
	private Object safeRemove(Reference key) {
		//System.out.print("=");
		// Need to use key.hashCode(), which is the old identityHashCode of the expired
		// weakref, rather than just hashIndex(key).
		int index = hashIndexFromCode(key.hashCode());
		HashEntry cur = data[index];
		HashEntry prev = null;
		while(cur != null) {
			if(((WeakKeyHashEntry)cur).getRawKey() == key) {
				Object old = cur.value;
				cur.live = false;
				size--;
				if(prev == null) {
					data[index] = cur.next;
				} else {
					prev.next = cur.next;
				}
				return old;
			}
			prev = cur;
			cur = cur.next;
		}
		
		/* With the centralised reference queue, it is actually possible to fall through without finding
		 * a match. When this happens, we just return null.  If entries seem to be disappearing, it's
		 * possible somewhere the wrong hashcode is used; uncomment this for diagnostics.
		 
		
		try { throw new RuntimeException("safeRemove failed"); } catch(Exception e) { e.printStackTrace(); }
		for(int i = 0; i < data.length; i++) {
			for(cur = data[i]; cur != null; cur = cur.next) {
				if(((WeakKeyHashEntry)cur).getRawKey() == key) {
					System.out.println("Attention -- found matching key at hash index " + i + ", rather than the expected " + index);
				}
			}
		} */
		return null;
	}
}
