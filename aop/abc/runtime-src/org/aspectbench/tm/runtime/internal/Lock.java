package org.aspectbench.tm.runtime.internal;

/* abc - The AspectBench Compiler
 * Copyright (C) 2006 Julian Tibble
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

import java.lang.InterruptedException;

public class Lock
{
    protected Thread owner = null;

    /**
     * Get the lock and record the current thread
     * as the owner it.
     */
    synchronized public void get()
    {
        while (owner != null) {
            try {
                this.wait();
            } catch (InterruptedException e) { }
        }

        owner = Thread.currentThread();
    }

    /**
     * Returns true iff the current thread owns the lock.
     */
    public boolean own()
    {
        return owner == Thread.currentThread();
    }

    /**
     * Release the lock if the current thread owns it,
     * otherwise do nothing.
     */
    synchronized public void release()
    {
        if (owner == Thread.currentThread()) {
            owner = null;
            this.notify();
        }
    }
}
