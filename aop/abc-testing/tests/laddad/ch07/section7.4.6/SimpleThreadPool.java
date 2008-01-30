//Listing 7.14 SimpleThreadPool.java

import java.util.*;

public class SimpleThreadPool implements ThreadPool {
    List _waitingThreads = new ArrayList();

    public boolean putThread(Thread thread) {
	assert Thread.currentThread() == thread;
	synchronized(thread) {
	    synchronized (this) {
		_waitingThreads.add(thread);
	    }
	    try {
		thread.wait();
	    } catch(InterruptedException ex) {
	    }
	}
	return true;
    }

    synchronized public Thread getThread() {
	if (!_waitingThreads.isEmpty()) {
	    Thread availableThread
		= (Thread)_waitingThreads.remove(0);
	    return availableThread;
	}
	return null;
    }

    public boolean wakeupThread(Thread thread) {
	if (thread.isAlive()) {
	    synchronized(thread) {
		thread.notify();
		return true;
	    }
	}
	return false;
    }
}

