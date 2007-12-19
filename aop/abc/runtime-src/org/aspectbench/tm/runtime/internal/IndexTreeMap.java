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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A class implementing an index tree with fully collectable indices,
 * as well as methods to easily add and get values. Note that keys for
 * all levels must be specified, there is no way to iterate over a keyset.
 * 
 * @author Pavel Avgustinov
 */
public class IndexTreeMap implements IndexTree {
	/**
	 * Class for the maps constituting each level of the IndexTreeMap.
	 * Essentially, those are weak-key collecting hashmaps with some
	 * extra cleanup functionality.
	 */
	static class IndexTreeLevelMap extends WeakKeyCollectingIdentityHashMap {
		private IndexTreeLevelMap parent;
		private Object parentKey;
		
		public IndexTreeLevelMap(IndexTreeLevelMap parent, Object key) {
			this.parent = parent;
			this.parentKey = key;
		}
		
		/**
		 * Perform the cleanup after a particular key of this map has become empty:
		 * check if the whole map is empty, and if it is, tell the parent to drop it.
		 */
		public void cleanupForKey(Object key) {
			remove(key);
			if(parent != null && isEmpty()) {
				parent.cleanupForKey(parentKey);
				parent = null;
			}
		}
	}
	
	private int depth;
	private Map map;
	private Set set;
	
	public IndexTreeMap(int depth) {
		this.depth = depth;
		if (depth == 0)
			this.set = getNewBindingSet(null, null);
		else
			this.map = getNewComponentMap(null, null);
	}
	
	/**
	 * Construct one of the component maps that hold each level of the IndexTree
	 */
	private static Map getNewComponentMap(IndexTreeLevelMap parent, Object key) {
		return new IndexTreeLevelMap(parent, key);
	}
	
	/**
	 * Construct one of the sets at the leaves of the IndexTree
	 */
	private static Set getNewBindingSet(IndexTreeLevelMap parent, Object key) {
		return new IndexTreeLeafSet(parent, key);
	}
	
	/**
	 * Get an iterator over all values associated with a particular set of keys.
	 * 
	 * For performance, lots of runtime checks are omitted -- keys is assumed
	 * to have the right size, we assume things are appropriately castable etc.
	 * Since this is an internal runtime class, we don't expect people who don't
	 * abide by the contracts to use it.
	 */
	public Iterator get(Object[] keys) {
		if (depth == 0)
			return set.iterator();

		Object cur = map;
		for(int i = 0; i < depth; i++) {
			cur = ((Map)cur).get(keys[i]);
			if(cur == null) return null;
		}
		return ((Set)cur).iterator();
	}

	/**
	 * Insert a particular ITDBinding into the IndexTree, at a leaf given by
	 * the set of keys. Maps that currently don't exist are created.
	 */
	public void insert(Object[] keys, MaybeWeakRef value) {
		if (depth == 0) {
			set.add(value);
			return;
		}

		Map cur = map;
		Map next;
		Set set;
		for(int i = 0; i < depth - 1; i++) {
			next = (Map)cur.get(keys[i]);
			if(next == null) {
				// create all the map levels
				for(int j = i; j < depth - 1; j++) {
					next = getNewComponentMap((IndexTreeLevelMap)cur, keys[j]);
					cur.put(keys[j], next);
					cur = next;
				}
				set = getNewBindingSet((IndexTreeLevelMap) cur, keys[depth-1]);
				cur.put(keys[depth - 1], set);
				set.add(value);
				return;
			}
			cur = next;
		}
		set = (Set)cur.get(keys[depth - 1]);
		if(set == null) {
			set = getNewBindingSet((IndexTreeLevelMap) cur, keys[depth-1]);
			cur.put(keys[depth - 1], set);
		}
		set.add(value);
	}

	/*
	 * Is it necessary to have a cleanup method? The idea is for IndexTreeLeafs to register
	 * themselves as containers to weakrefs, who then notify them of expiry...
	public void cleanup() {
		
		throw new RuntimeException("Shouldn't call cleanup directly");
	}
	*/
}
