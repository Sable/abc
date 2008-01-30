//Listing 7.16 Logging the pool operations: ThreadPoolLoggingAspect

public aspect ThreadPoolLoggingAspect {
    after() returning(Thread thread)
	: execution(Thread ThreadPool.getThread(..)) {
	System.out.println("Got from pool: " + thread);
    }

    before(Thread thread)
	: execution(boolean ThreadPool.putThread(Thread))
	&& args(thread) {
	System.out.println("Putting in pool: " + thread + "\n");
    }

    before(Thread thread)
	: execution(boolean ThreadPool.wakeupThread(Thread))
	&& args(thread) {
	System.out.println("Waking up: " + thread);
    }
}
