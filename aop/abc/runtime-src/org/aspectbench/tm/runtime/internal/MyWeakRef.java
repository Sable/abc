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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class MyWeakRef extends WeakReference {
	private int hashCode;
	public MyWeakRef(Object arg0) {
		super(arg0);
		hashCode = System.identityHashCode(arg0);
	}

	public MyWeakRef(Object arg0, ReferenceQueue arg1) {
		super(arg0, arg1);
		hashCode = System.identityHashCode(arg0);
	}

	/**
	 * A MyWeakRef is equal to an Object if either the Object is a MyWeakRef and there's reference
	 * equality between the referents, or the Object isn't a MyWeakRef, and there's reference equality
	 * between that object and the referent of this MyWeakRef.
	 */
	public boolean equals(Object obj) {
		if(this==obj) {
			return true;
		}
		if(obj instanceof MyWeakRef) {
			return this.get() == ((MyWeakRef)obj).get();
		} else {
			return this.get() == obj;
		}
	}

	public int hashCode() {
		return hashCode;
	}
	
	public boolean isExpired() {
		return get() == null;
	}
}
