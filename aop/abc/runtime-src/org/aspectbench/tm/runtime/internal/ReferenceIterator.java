package org.aspectbench.tm.runtime.internal;

import java.util.Iterator;

/**
 * A wrapper around an iterator of one of the *IdentityMap keysets. It assumes the underlying
 * collection consists of MyWeakRef objects, and dereferences each one before returning it
 * in the next() method.
 * 
 * @author Pavel Avgustinov
 */

public class ReferenceIterator implements Iterator {
	Iterator realIt;
	
	public ReferenceIterator(Iterator it) {
		realIt = it;
	}
	
	public boolean hasNext() {
		return realIt.hasNext();
	}

	public Object next() {
		return ((MyWeakRef)realIt.next()).get();
	}

	public void remove() {
		realIt.remove();
	}

}
