//Listing 8.19 ConcernAspect.java: with advice that catches all exceptions

public abstract aspect ConcernAspect {
    abstract pointcut operations();

    Object around() : operations() {
	try {
	    return proceed();
	} catch (Throwable ex) {
	    // do something
	    throw new ConcernRuntimeException(ex);
	}
    }
}
