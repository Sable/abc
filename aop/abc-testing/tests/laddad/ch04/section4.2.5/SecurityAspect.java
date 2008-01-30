//Listing 4.12 SecurityAspect.java

public aspect SecurityAspect {
    private String TestPrecedence._id
	= "SecurityAspect:id";

    private void TestPrecedence.printId() {
	System.out.println(
	   "<SecurityAspect:performSecurityCheck id=" + _id + "/>");
    }

    public pointcut performCall() : call(* TestPrecedence.perform());

    before(TestPrecedence test) : performCall() && target(test) {
	System.out.println("<SecurityAspect:before/>");
	System.out.println(test._id);
	test.printId();
    }
}

