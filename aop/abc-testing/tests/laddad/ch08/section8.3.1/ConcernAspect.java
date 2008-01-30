//Listing 8.13 ConcernAspect.java

public abstract aspect ConcernAspect {
    abstract pointcut operations();
    before() throws ConcernCheckedException : operations() {
	concernLogic();
    }

    void concernLogic() throws ConcernCheckedException {
	throw new ConcernCheckedException(); // simulating failure
    }
}
