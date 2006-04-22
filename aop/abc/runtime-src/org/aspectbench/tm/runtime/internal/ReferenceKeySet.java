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
import java.util.Iterator;
import java.util.Set;

/**
 * A wrapper around the keySet of one of the *IdentityMap classes. Effectively it only limits what
 * can be done with the keySet and wraps the iterator in a ReferenceIterator.
 * 
 * @author Pavel Avgustinov
 */

public class ReferenceKeySet implements Set {
	Set realSet;
	
	public ReferenceKeySet(Set set) {
		realSet = set;
	}

	public int size() {
		return realSet.size();
	}

	public boolean isEmpty() {
		return realSet.isEmpty();
	}

	public boolean contains(Object o) {
		return realSet.contains(o);
	}

	public Iterator iterator() {
		return new ReferenceIterator(realSet.iterator());
	}

	public Object[] toArray() {
		throw new RuntimeException("ReferenceKeySet.toArray would yield undesirable results (either array of strong references or" +
				"array of reference boxes).");
	}

	public Object[] toArray(Object[] arg0) {
		throw new RuntimeException("ReferenceKeySet.toArray would yield undesirable results (either array of strong references or" +
		"array of reference boxes).");
	}

	public boolean add(Object arg0) {
		throw new RuntimeException("Adding elements to a ReferenceKeySet is not well-defined.");
	}

	public boolean remove(Object o) {
		throw new RuntimeException("Removing elements of a ReferenceKeySet is not well-defined.");
	}

	public boolean containsAll(Collection arg0) {
		throw new RuntimeException("containsAll on a ReferenceKeySet is not well-defined.");
	}

	public boolean addAll(Collection arg0) {
		throw new RuntimeException("Adding elements to a ReferenceKeySet is not well-defined.");
	}

	public boolean retainAll(Collection arg0) {
		throw new RuntimeException("Removing elements of a ReferenceKeySet is not well-defined.");
	}

	public boolean removeAll(Collection arg0) {
		throw new RuntimeException("Removing elements of a ReferenceKeySet is not well-defined.");
	}

	public void clear() {
		realSet.clear();
	}

}
