//Listing 7.11 The thread pool interface

public interface ThreadPool {
    public boolean putThread(Thread thread);
    public Thread getThread();
    public boolean wakeupThread(Thread thread);
}

