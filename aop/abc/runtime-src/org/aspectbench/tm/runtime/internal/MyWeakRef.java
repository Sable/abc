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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class MyWeakRef extends WeakReference {
	// Global queue for MyWeakRefs and subclasses
	public static ReferenceQueue expiredQueue = new ReferenceQueue();
	
	// Poll the global queue and call the cleanup() method on any expired reference
	public static void checkExpired() {
		Reference expired = expiredQueue.poll();
		while (expired != null) {
			((MyWeakRef) expired).cleanup();
			expired = expiredQueue.poll();
		}
	}
	
	public void cleanup() {
		// do nothing; subclasses can override this
	}

	private int hashCode;
	public MyWeakRef(Object arg0) {
		super(arg0);
		hashCode = System.identityHashCode(arg0);
	}

	public MyWeakRef(Object arg0, ReferenceQueue arg1) {
		super(arg0, arg1);
		hashCode = System.identityHashCode(arg0);
	}
	
	public MyWeakRef(Object arg0, ReferenceQueue arg1, boolean trackContainers) {
		super(arg0, arg1);
		hashCode = System.identityHashCode(arg0);
		if(trackContainers)
			containers = new ArrayList();
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
		return super.get() == null;
	}
	
	/**
	 * MyWeakRefs can optionally be registered with WeakRefContainers; on expiry,
	 * one can notify all containers.
	 */
	private List containers = new LinkedList();
	
	/**
	 * Add a new container to the list 
	 */
	public void addContainer(WeakRefContainer c) {
		containers.add(c);
	}
	
	/**
	 * MyWeakRefs can't be disassociated from containers at this stage (doesn't
	 * seem necessary).
	 */
	public void removeContainer(WeakRefContainer c) {
		throw new UnsupportedOperationException("Can't disassociate container from weakref (yet?)");
	}
	
	/**
	 * Notify all containers of expiry, then drop all references to them.
	 */
	public void notifyContainers() {
		for (Iterator contIter = containers.iterator(); contIter.hasNext();) {
			WeakRefContainer c = (WeakRefContainer) contIter.next();
			c.weakrefExpired(this);
		}
		containers = null;
	}
}
