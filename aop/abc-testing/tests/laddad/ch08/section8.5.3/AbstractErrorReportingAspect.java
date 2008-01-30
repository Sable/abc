//Listing 8.30 AbstractErrorReportingAspect.java

public abstract aspect AbstractErrorReportingAspect {
    public abstract pointcut normalOperations();

    public abstract pointcut criticalOperations();

    after() throwing : normalOperations() || criticalOperations() {
	//... log the error
    }

    after() throwing : criticalOperations() {
	//... code to handle critical errors
	//... page, email, call, contact by all available means
    }
}
