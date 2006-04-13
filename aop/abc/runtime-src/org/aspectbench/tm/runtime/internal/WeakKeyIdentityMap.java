package org.aspectbench.tm.runtime.internal;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

/**
 * A Map implementation that uses reference identity of keys and holds weak references to its keys.
 * 
 * No purging of invalidated keys takes place.
 * 
 * Large parts of the Map interface aren't implemented as they are not necessary for the intended 
 * application (tracematch indexing) and would be rather less than straightforward to implement.
 * 
 * @author Pavel Avgustinov
 */

public class WeakKeyIdentityMap implements Map {
	HashMap map = new HashMap();
	
	private Object notImplemented(String s) { 
		throw new RuntimeException("Method " + s + " is not implemented for WeakKeyIdentityMap -- it shouldn't be used. " +
				"Submit stack trace if you really need it.");
	}
	
	protected Object readBox(Object key) {
		return new MyWeakRef(key);
	}
	
	protected Object writeBox(Object key) {
		return new MyWeakRef(key);
	}

	public int size() {
		return map.size();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public boolean containsKey(Object key) {
		return map.containsKey(readBox(key));
	}

	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	public Object get(Object key) {
		return map.get(readBox(key));
	}

	public Object put(Object key, Object value) {
		return map.put(writeBox(key), value);
	}

	public Object remove(Object key) {
		return map.remove(readBox(key));
	}
	
	public void putAll(Map arg0) {
		notImplemented("putAll");
	}

	public void clear() {
		map.clear();
	}

	public Set keySet() {
		return new ReferenceKeySet(map.keySet());
	}

	public Collection values() {
		return (Collection)notImplemented("values");
	}

	public Set entrySet() {
		return (Set)notImplemented("entrySet");
	}

}
