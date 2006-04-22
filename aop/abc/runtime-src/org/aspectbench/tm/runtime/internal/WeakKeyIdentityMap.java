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
