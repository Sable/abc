//Listing 4.9 SecurityAspect.java

public abstract aspect SecurityAspect {
    public pointcut performCall() :
	call(* TestPrecedence.perform());
    before() : performCall() {
	System.out.println("<SecurityAspect:check/>");
    }
}
