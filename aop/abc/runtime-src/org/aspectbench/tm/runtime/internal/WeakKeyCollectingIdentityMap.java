package org.aspectbench.tm.runtime.internal;

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

import java.util.Set;
import java.lang.ref.ReferenceQueue;

/**
 * A Map implementation that uses reference identity of keys and holds weak references to its keys.
 * 
 * When a key is garbage-collected, the corresponding key->value mapping is dropped. Note that this
 * cleanup only happens upon certain map operations, so if they never occur the mapping will never
 * be cleaned up. Periodically call isEmpty() (or somesuch) if you need to guarantee mappings expire.
 * 
 * There's a potential race condition with the keySet method -- due to the above-mentioned cleanup,
 * mappings may be removed even without explicit user intervention while the keyset is being iterated.
 * 
 * Large parts of the Map interface aren't implemented as they are not necessary for the intended 
 * application (tracematch indexing) and would be rather less than straightforward to implement.
 * 
 * @author Pavel Avgustinov
 */

public class WeakKeyCollectingIdentityMap extends WeakKeyIdentityMap {
	ReferenceQueue queue = new ReferenceQueue();

	protected Object writeBox(Object key) {
		return new MyWeakRef(key, queue);
	}
	
	protected void purge() {
		Object ref = queue.poll();
		while(ref != null) {
			this.map.remove(ref);
			ref = queue.poll();
		}
	}

	public void clear() {
		purge();
		super.clear();
	}

	public Object get(Object key) {
		purge();
		return super.get(key);
	}

	public boolean isEmpty() {
		purge();
		return super.isEmpty();
	}

	public Set keySet() {
		purge();
		return super.keySet();
	}

	public Object put(Object key, Object value) {
		purge();
		return super.put(key, value);
	}

	public Object remove(Object key) {
		purge();
		return super.remove(key);
	}

	public int size() {
		purge();
		return super.size();
	}

}
