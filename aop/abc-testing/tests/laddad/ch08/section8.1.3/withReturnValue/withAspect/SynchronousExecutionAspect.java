//Listing 8.7 SynchronousExecutionAspect.java

import pattern.worker.*;

public abstract aspect SynchronousExecutionAspect {
    public abstract pointcut syncOperations();

    Object around() : syncOperations() {
	RunnableWithReturn worker = new RunnableWithReturn() {
		public void run() {
		    _returnValue = proceed();
		}};
	System.out.println("About to run " + worker);
	worker.run();
	return worker.getReturnValue();
    }
}
