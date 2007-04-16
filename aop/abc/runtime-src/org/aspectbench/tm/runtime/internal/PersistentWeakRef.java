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

import java.lang.ref.WeakReference;

public class PersistentWeakRef extends WeakReference {
	private static WeakKeyCollectingIdentityHashMap refMap = new WeakKeyCollectingIdentityHashMap();
	private int hashCode;
	
	public static WeakReference getWeakRef(Object o) {
		WeakReference ref = (WeakReference)refMap.get(o);
		if(ref == null) {
			ref = new PersistentWeakRef(o);
			refMap.put(o, ref);
		}
		return ref;
	}
	
	protected PersistentWeakRef(Object ref) {
		super(ref);
		hashCode = System.identityHashCode(ref);
	}
	
	/**
	 * Only one PersistentWeakRef is ever constructed for a particular object. Thus,
	 * this PWR is equal to obj iff they are the same weak reference, or if obj is
	 * the referent of this.
	 */
	public boolean equals(Object obj) {
		return this == obj || this.get() == obj;
	}

	/**
	 * Return the referent while it's alive; after it is destroyed, return this.
	 * TODO: Is it sound to always return 'this' instead?
	 */
	public Object get() {
		Object result = super.get();
		if(result == null) result = this;
		return result;
	}
}