/* ******************************************
 CFlowCounter (runtime):

 Manage state of a cflow by a counter instead
 of a stack if there are no free variables

 to be used instead of 
    org.aspectj.internal.CFlowStack
 when possible in woven code
 ******************************************* */

package uk.ac.ox.comlab.abc.runtime.internal;

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
	    // Should this check for overflow?

	    c = c + 1;
	}

        public void dec() {
	    // assuming correctness of the weaver, no need
	    // to check that c /= 0

            c = c-1;
	}

        public boolean isZero() {
            return (c == 0);
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
}
