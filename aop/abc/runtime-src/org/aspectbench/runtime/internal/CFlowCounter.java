/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 *               2004 Damien Sereni
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 *     Damien Sereni  modified for cflow counters
 * ******************************************************************/

/* ******************************************
 CFlowCounter (runtime):

 Manage state of a cflow by a counter instead
 of a stack if there are no free variables

 to be used instead of 
    org.aspectj.internal.CFlowStack
 when possible in woven code
 ******************************************* */

package org.aspectbench.runtime.internal;

import java.util.Stack;
import java.util.Hashtable;
import java.util.Enumeration;

public class CFlowCounter {

    private Hashtable counters = new Hashtable();
    private Thread cached_thread;
    private Counter cached_counter;
    private int change_count = 0;
    private static final int COLLECT_AT = 20000;
    private static final int MIN_COLLECT_AT = 100;

    private static class Counter {
	// A simple counter class

	private int c;

        public Counter() {
	    c = 0;
	}

        public void inc() {

	    c = c + 1;
	}

        public void dec() {

            c = c-1;
	}

        public boolean isZero() {
            return (c == 0);
	}

        public int count() {
            return c;
	}
    }

    private synchronized Counter getThreadCounter() {

	// Keep a counter for each thread:
	// Adapted from org.aspectj.runtime.internal.CFlowStack

        if (Thread.currentThread() != cached_thread) {
            cached_thread = Thread.currentThread();
            cached_counter = (Counter)counters.get(cached_thread);
            if (cached_counter == null) {
                cached_counter = new Counter();
                counters.put(cached_thread, cached_counter);
            }
            change_count++;
            // Collect more often if there are many threads, but not *too* often
            int size = Math.max(1, counters.size()); // should be >1 b/c always live threads, but...
            if (change_count > Math.max(MIN_COLLECT_AT, COLLECT_AT/size)) {
                Stack dead_counters = new Stack();
                for (Enumeration e = counters.keys(); e.hasMoreElements(); ) {
                    Thread t = (Thread)e.nextElement();
                    if (!t.isAlive()) dead_counters.push(t);
                }
                for (Enumeration e = dead_counters.elements(); e.hasMoreElements(); ) {
                    Thread t = (Thread)e.nextElement();
                    counters.remove(t);
                }
                change_count = 0;
            }
        }
        return cached_counter;
    }

    public void inc() {
	getThreadCounter().inc();
   }

    public void dec() {
	getThreadCounter().dec();
    }

    public boolean isValid() {
	/* isValid() : check whether the cflow applies in the 
	   current state */
        return (!getThreadCounter().isZero());
    }

    // Getting a handle on the counter for the current thread
    // Return an Object to ensure that the Counter class can be changed

    public Object getCounter() {
	return getThreadCounter();
    }

    // If we already have a handle on the counter (no need for a getThreadCounter()):
    // Note: parameter will always be a return value of getCounter(), so really a Counter

    public static final void incCounter(Object c) {
	((Counter)c).inc();
    }

    public static final void decCounter(Object c) {
	((Counter)c).dec();
    }

    public static final boolean isValidCounter(Object c) {
	return (!((Counter)c).isZero());
    }
    public static final int depthCounter(Object c) {
	return ((Counter)c).count();
    }

}
