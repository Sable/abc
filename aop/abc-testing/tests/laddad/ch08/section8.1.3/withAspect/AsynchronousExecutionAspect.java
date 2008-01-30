//Listing 8.4 AsynchronousExecutionAspect.java

public abstract aspect AsynchronousExecutionAspect {
    public abstract pointcut asyncOperations();

    void around() : asyncOperations() {
	Runnable worker = new Runnable() {
		public void run() {
		    proceed();
		}
	    };
	Thread asyncExecutionThread = new Thread(worker);
	asyncExecutionThread.start();
    }
}
