package org.aspectbench.tm.runtime.internal;

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
