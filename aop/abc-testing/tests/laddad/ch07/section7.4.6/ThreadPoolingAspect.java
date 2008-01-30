//Listing 7.13 ThreadPoolingAspect.java

public aspect ThreadPoolingAspect {
    ThreadPool _pool = new SimpleThreadPool();

    pointcut threadCreation(Runnable worker)
	: call(Thread.new(Runnable)) && args(worker)
	&& args(EchoWorker);

    pointcut session(DelegatingThread thread)
	: execution(void DelegatingThread.run()) && this(thread);

    pointcut threadStart(DelegatingThread thread)
	: call(void Thread.start()) && target(thread);

    Thread around(Runnable worker) : threadCreation(worker) {
	DelegatingThread availableThread
	    = (DelegatingThread)_pool.getThread();
	if (availableThread == null) {
	    availableThread = new DelegatingThread();
	}
	availableThread.setDelegatee(worker);
	return availableThread;
    }

    void around(DelegatingThread thread) : session(thread) {
	while (true) {
	    proceed(thread);
	    _pool.putThread(thread);
	}
    }

    void around(Thread thread) : threadStart(thread) {
	if (!_pool.wakeupThread(thread)) {
	    proceed(thread);
	}
    }
}

