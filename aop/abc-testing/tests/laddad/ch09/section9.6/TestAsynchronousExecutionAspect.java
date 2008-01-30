//Listing 9.14 TestAsynchronousExecutionAspect.java

public aspect TestAsynchronousExecutionAspect
    extends AsynchronousExecutionAspect {
    public pointcut asyncOperations()
	: call(void sendEmails());
}
