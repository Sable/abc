//Listing 8.17 ConcernAspect.java: modified to use the exception introduction pattern

public abstract aspect ConcernAspect {
    abstract pointcut operations();

    before() : operations() {
	try {
	    concernLogic();
	} catch (ConcernCheckedException ex) {
	    throw new ConcernRuntimeException(ex);
	}
    }

    void concernLogic() throws ConcernCheckedException {
	throw new ConcernCheckedException(); // simulating failure
    }
}
