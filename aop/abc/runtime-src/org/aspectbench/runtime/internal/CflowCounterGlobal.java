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
import org.aspectbench.runtime.internal.cflowinternal.Counter;

public class CflowCounterGlobal implements CflowCounterInterface {

	// In the single-threaded case, the counter can be stored uniquely for the cflow:
	public int singleThreadedCount = 0;
	
    private Hashtable counters = new Hashtable();
    private Thread cached_thread;
    private Counter cached_counter;
    private int change_count = 0;
    private static final int COLLECT_AT = 20000;
    private static final int MIN_COLLECT_AT = 100;

    public synchronized Counter getThreadCounter() {

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

}
