package org.aspectbench.tm.runtime.internal;

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
}
