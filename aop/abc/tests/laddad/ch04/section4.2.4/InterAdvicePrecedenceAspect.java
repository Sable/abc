//Listing 4.11 InterAdvicePrecedenceAspect.java: testing advice ordering in a single aspect

public aspect InterAdvicePrecedenceAspect {
    public pointcut performCall() : call(* TestPrecedence.perform());

    after() returning : performCall() {
	System.out.println("<after1/>");
    }

    before() : performCall() {
	System.out.println("<before1/>");
    }

    void around() : performCall() {
	System.out.println("<around>");
	proceed();
	System.out.println("</around>");
    }

    before() : performCall() {
	System.out.println("<before2/>");
    }
}
