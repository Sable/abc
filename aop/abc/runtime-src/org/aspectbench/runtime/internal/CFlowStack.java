/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation
 * 	   Damien Sereni  added methods to cache the stack for the 
 * 					  current thread 
 * ******************************************************************/


package org.aspectbench.runtime.internal;

import org.aspectj.lang.NoAspectBoundException;
import org.aspectbench.runtime.CFlow;

import java.util.Stack;
import java.util.Hashtable;
import java.util.Enumeration;

public class CFlowStack {
    private Hashtable stacks = new Hashtable();
    private Thread cached_thread;
    private Stack cached_stack;
    private int change_count = 0;
    private static final int COLLECT_AT = 20000;
    private static final int MIN_COLLECT_AT = 100;
    /*    private static final int MAX_STACK_TRACES = 100;

    private int stack_traces = 0;*/
    
    private synchronized Stack getThreadStack() {
        if (Thread.currentThread() != cached_thread) {
            cached_thread = Thread.currentThread();
            cached_stack = (Stack)stacks.get(cached_thread);
            if (cached_stack == null) {
                cached_stack = new Stack();
                stacks.put(cached_thread, cached_stack);
            }
            change_count++;
            // Collect more often if there are many threads, but not *too* often
            int size = Math.max(1, stacks.size()); // should be >1 b/c always live threads, but...
            if (change_count > Math.max(MIN_COLLECT_AT, COLLECT_AT/size)) {
                Stack dead_stacks = new Stack();
                for (Enumeration e = stacks.keys(); e.hasMoreElements(); ) {
                    Thread t = (Thread)e.nextElement();
                    if (!t.isAlive()) dead_stacks.push(t);
                }
                for (Enumeration e = dead_stacks.elements(); e.hasMoreElements(); ) {
                    Thread t = (Thread)e.nextElement();
                    stacks.remove(t);
                }
                change_count = 0;
            }
        }
        return cached_stack;
    }

	//XXX dangerous, try to remove
    public void push(Object obj) {
	/*	if (stack_traces < MAX_STACK_TRACES) {
	    System.out.println("Push (obj)");
	    stack_traces = stack_traces+1;
	    Thread.dumpStack();
	    }*/
        getThreadStack().push(obj);
    }

    public void pushInstance(Object obj) {
	/*	if (stack_traces < MAX_STACK_TRACES) {
	    System.out.println("PushInstance (obj)");
	    stack_traces = stack_traces+1;
	    Thread.dumpStack();
	    }*/
        getThreadStack().push(new CFlow(obj));
    }

    public void push(Object[] obj) {
	/*	if (stack_traces < MAX_STACK_TRACES) {
	    System.out.println("Push (obj[])");
	    stack_traces = stack_traces+1;
	    Thread.dumpStack();
	    }*/
        getThreadStack().push(new CFlowPlusState(obj));
    }

    public void pop() {
	/*	if (stack_traces < MAX_STACK_TRACES) {
	    System.out.println("Pop()");
	    stack_traces = stack_traces+1;
	    Thread.dumpStack();
	    }*/
        getThreadStack().pop();
    }

    public Object peek() {
        Stack stack = getThreadStack();
        if (stack.isEmpty()) throw new org.aspectj.lang.NoAspectBoundException();
        return (Object)stack.peek();
    }
    
    public Object get(int index) {
        CFlow cf = peekCFlow();
        return (null == cf ? null : cf.get(index));
    }

    public Object peekInstance() {
    	CFlow cf = peekCFlow();
    	if (cf != null ) return cf.getAspect();
    	else throw new NoAspectBoundException();
    }

    public CFlow peekCFlow() {
        Stack stack = getThreadStack();
        if (stack.isEmpty()) return null;
        return (CFlow)stack.peek();
    }

    public CFlow peekTopCFlow() {
        Stack stack = getThreadStack();
        if (stack.isEmpty()) return null;
        return (CFlow)stack.elementAt(0);
    }

    public boolean isValid() {
        return !getThreadStack().isEmpty();
    }

    // Getting a handle on the stack for the current thread
    // Returned as Object so can't depend on it actually being a Stack

    public Object getStack() {
	return getThreadStack();
    }

    // Stack operations if we already know the stack for the current thread
    // Only gets passed return values of getStack(), so all OK

    public static final void pushInstanceStack(Object obj, Object stack) {
        ((Stack)stack).push(new CFlow(obj));
    }

    public static final void pushStack(Object[] obj, Object stack) {
        ((Stack)stack).push(new CFlowPlusState(obj));
    }

    public static final void popStack(Object stack) {
        ((Stack)stack).pop();
    }

    public static final Object peekStack(Object stack) {
        if (((Stack)stack).isEmpty()) throw new org.aspectj.lang.NoAspectBoundException();
        return (Object)((Stack)stack).peek();
    }
    
    public static final Object getTopStack(int index, Object stack) {
        CFlow cf = peekCFlowStack(stack);
        return (null == cf ? null : cf.get(index));
    }

    public static final Object peekInstanceStack(Object stack) {
    	CFlow cf = peekCFlowStack(stack);
    	if (cf != null ) return cf.getAspect();
    	else throw new NoAspectBoundException();
    }

    public static final CFlow peekCFlowStack(Object stack) {
        if (((Stack)stack).isEmpty()) return null;
        return (CFlow)((Stack)stack).peek();
    }

    public static final CFlow peekTopCFlowStack(Object stack) {
        if (((Stack)stack).isEmpty()) return null;
        return (CFlow)((Stack)stack).elementAt(0);
    }

    public static final boolean isValidStack(Object stack) {
        return !((Stack)stack).isEmpty();
    }

    public static final int depthStack(Object stack) {
        return ((Stack)stack).size();
    }

}
