package org.aspectbench.tm.runtime.internal;

import java.lang.InterruptedException;

public class Lock
{
    protected boolean locked = false;
    protected Thread owner = null;

    /**
     * Get the lock and record the current thread
     * as the owner it.
     */
    synchronized public void get()
    {
        while (locked) {
            try {
                this.wait();
            } catch (InterruptedException e) { }
        }

        locked = true;
        owner = Thread.currentThread();
    }

    /**
     * Returns true iff the current thread owns the lock.
     */
    synchronized public boolean own()
    {
        return locked && owner == Thread.currentThread();
    }

    /**
     * Release the lock if the current thread owns it,
     * otherwise do nothing.
     */
    synchronized public void release()
    {
        if (locked && owner == Thread.currentThread()) {
            locked = false;
            this.notify();
        }
    }
}
