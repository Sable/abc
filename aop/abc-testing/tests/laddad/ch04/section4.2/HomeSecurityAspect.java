//Listing 4.4 HomeSecurityAspect.java

public aspect HomeSecurityAspect {
    before() : call(void Home.exit()) {
	System.out.println("Engaging");
    }

    after() : call(void Home.enter()) {
	System.out.println("Disengaging");
    }
}
