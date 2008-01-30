//Listing 8.29 Tracing.java

public aspect Tracing {
    before() : call(* *.*(..)) {
	System.out.println("Calling: " + thisJoinPointStaticPart);
    }
}
