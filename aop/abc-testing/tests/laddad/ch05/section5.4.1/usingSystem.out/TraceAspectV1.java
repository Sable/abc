//Listing 5.10 Tracing methods: the first version, using System.out as the logging stream

import org.aspectj.lang.*;

public aspect TraceAspectV1 {
    pointcut traceMethods()
	: (execution(* *.*(..))
	   || execution(*.new(..))) && !within(TraceAspectV1);

    before() : traceMethods() {
	Signature sig = thisJoinPointStaticPart.getSignature();
	System.out.println("Entering ["
			   + sig.getDeclaringType().getName() + "."
			   + sig.getName() + "]");
    }
}

