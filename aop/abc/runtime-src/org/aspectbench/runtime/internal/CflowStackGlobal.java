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
 *     Damien Sereni  modified for faster implementation of stacks
 * ******************************************************************/

package org.aspectbench.runtime.internal;

import java.util.Hashtable;
import java.util.Enumeration;
import org.aspectbench.runtime.internal.cflowinternal.*;

public class CflowStackGlobal {

    private static final int MIN_COLLECT_AT = 100; 
    private static final int COLLECT_AT = 20000;
    
    // General Stuff
    
    private static synchronized void garbageCollect(int change_count, Hashtable stacks) {
        // Collect more often if there are many threads, but not *too* often
        int size = Math.max(1, stacks.size()); // should be >1 b/c always live threads, but...
        if (change_count > Math.max(MIN_COLLECT_AT, COLLECT_AT/size)) {
            java.util.Stack dead_stacks = new java.util.Stack();
            for (Enumeration e = stacks.keys(); e.hasMoreElements(); ) {
                Thread t = (Thread)e.nextElement();
                if (!t.isAlive()) dead_stacks.push(t);
            }
            for (Enumeration e = dead_stacks.elements(); e.hasMoreElements(); ) {
                Thread t = (Thread)e.nextElement();
                stacks.remove(t);
            }
        }
    }

    // Stack classes. Specialised for primitive types, plus one for ref types
    // Note: boolean, char, byte and short are simply stored in the Int class
    public static class CflowStackRef implements CflowStackInterface.Ref {
        private StackRef cached_stack;
        private Thread cached_thread;
        private Hashtable stacks = new Hashtable();
        private int change_count = 0;

        public StackRef.Cell singleThreadedStack = null;
        
        public synchronized StackRef getThreadStack() {
            if (Thread.currentThread() != cached_thread) {
                cached_thread = Thread.currentThread();
                cached_stack = (StackRef)stacks.get(cached_thread);
                if (cached_stack == null) {
                    cached_stack = new StackRef();
                    stacks.put(cached_thread, cached_stack);
                }
                change_count++;
                garbageCollect(change_count, stacks);
                change_count = 0;
            }
            return cached_stack;
        }
    }
    public static class CflowStackInt implements CflowStackInterface.Int {
        private StackInt cached_stack;
        private Thread cached_thread;
        private Hashtable stacks = new Hashtable();
        private int change_count = 0;

        public StackInt.Cell singleThreadedStack = null;

        public synchronized StackInt getThreadStack() {
            if (Thread.currentThread() != cached_thread) {
                cached_thread = Thread.currentThread();
                cached_stack = (StackInt)stacks.get(cached_thread);
                if (cached_stack == null) {
                    cached_stack = new StackInt();
                    stacks.put(cached_thread, cached_stack);
                }
                change_count++;
                garbageCollect(change_count, stacks);
                change_count = 0;
            }
            return cached_stack;
        }
    }
    public static class CflowStackLong implements CflowStackInterface.Long {
        private StackLong cached_stack;
        private Thread cached_thread;
        private Hashtable stacks = new Hashtable();
        private int change_count = 0;
        
        public StackLong.Cell singleThreadedStack = null;

        public synchronized StackLong getThreadStack() {
            if (Thread.currentThread() != cached_thread) {
                cached_thread = Thread.currentThread();
                cached_stack = (StackLong)stacks.get(cached_thread);
                if (cached_stack == null) {
                    cached_stack = new StackLong();
                    stacks.put(cached_thread, cached_stack);
                }
                change_count++;
                garbageCollect(change_count, stacks);
                change_count = 0;
            }
            return cached_stack;
        }
    }
    public static class CflowStackFloat implements CflowStackInterface.Float {
        private StackFloat cached_stack;
        private Thread cached_thread;
        private Hashtable stacks = new Hashtable();
        private int change_count = 0;

        public StackFloat.Cell singleThreadedStack = null;

        public synchronized StackFloat getThreadStack() {
            if (Thread.currentThread() != cached_thread) {
                cached_thread = Thread.currentThread();
                cached_stack = (StackFloat)stacks.get(cached_thread);
                if (cached_stack == null) {
                    cached_stack = new StackFloat();
                    stacks.put(cached_thread, cached_stack);
                }
                change_count++;
                garbageCollect(change_count, stacks);
                change_count = 0;
            }
            return cached_stack;
        }
    }
    public static class CflowStackDouble implements CflowStackInterface.Double {
        private StackDouble cached_stack;
        private Thread cached_thread;
        private Hashtable stacks = new Hashtable();
        private int change_count = 0;

        public StackDouble.Cell singleThreadedStack = null;

        public synchronized StackDouble getThreadStack() {
            if (Thread.currentThread() != cached_thread) {
                cached_thread = Thread.currentThread();
                cached_stack = (StackDouble)stacks.get(cached_thread);
                if (cached_stack == null) {
                    cached_stack = new StackDouble();
                    stacks.put(cached_thread, cached_stack);
                }
                change_count++;
                garbageCollect(change_count, stacks);
                change_count = 0;
            }
            return cached_stack;
        }
    }

}
	