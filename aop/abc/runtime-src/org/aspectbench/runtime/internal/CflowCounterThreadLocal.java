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
 *     Laurie Hendren the ThreadLocal implementation
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

public class CflowCounterThreadLocal extends ThreadLocal implements CflowCounterInterface {

    private final Thread first_thread = Thread.currentThread();
    private final Counter first_counter = new Counter();

    public Object initialValue() {
     if (Thread.currentThread() == first_thread)
          return first_counter;
     return new Counter();
   }

   public Counter getThreadCounter() {
      if (Thread.currentThread() == first_thread)
        return first_counter;
      return (Counter) get();
   }

}
