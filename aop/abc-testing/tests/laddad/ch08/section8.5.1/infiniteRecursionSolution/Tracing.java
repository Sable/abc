//Listing 8.29 Tracing.java

public aspect Tracing {
    before() : call(* *.*(..)) && !within(Tracing) {
	System.out.println("Calling: " + thisJoinPointStaticPart);
    }
}

