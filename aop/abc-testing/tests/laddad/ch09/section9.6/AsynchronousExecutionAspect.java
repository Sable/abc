//Listing 9.13 AsynchronousExecutionAspect.java

import java.awt.EventQueue;
public abstract aspect AsynchronousExecutionAspect {
    public abstract pointcut asyncOperations();

    void around() : asyncOperations()
	&& if(EventQueue.isDispatchThread()) {
	    Runnable worker = new Runnable() {
		    public void run() {
			proceed();
		    }
		};
	    Thread asyncExecutionThread = new Thread(worker);
	    asyncExecutionThread.start();
	}
}
