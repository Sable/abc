//Listing 4.10 ExtendedSecurityAspect.java

public aspect ExtendedSecurityAspect extends SecurityAspect {
    before() : performCall() {
	System.out.println("<ExtendedSecurityAspect:check/>");
    }
}
