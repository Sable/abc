//Listing 9.16 ReadWriteLockSynchronizationAspect.java

import EDU.oswego.cs.dl.util.concurrent.*;

public abstract aspect ReadWriteLockSynchronizationAspect
    perthis(readOperations() || writeOperations()) {
    public abstract pointcut readOperations();

    public abstract pointcut writeOperations();

    private ReadWriteLock _lock
	= new ReentrantWriterPreferenceReadWriteLock();

    before() : readOperations() {
	_lock.readLock().acquire();
    }

    after() : readOperations() {
	_lock.readLock().release();
    }

    before() : writeOperations() {
	_lock.writeLock().acquire();
    }

    after() : writeOperations() {
	_lock.writeLock().release();
    }

    static aspect SoftenInterruptedException {
	declare soft : InterruptedException :
	    call(void Sync.acquire());
    }
}

