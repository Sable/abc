//Listing 5.19 TraceAspectV4.java: adding the indentation effect to the log

import org.aspectj.lang.*;

import logging.*;

public aspect TraceAspectV4 extends IndentedLogging {
    protected pointcut loggedOperations()
	: (execution(* *.*(..))
	   || execution(*.new(..))) && !within(IndentedLogging+);

    before() : loggedOperations() {
	Signature sig = thisJoinPointStaticPart.getSignature();
	System.out.println("Entering ["
			   + sig.getDeclaringType().getName() + "."
			   + sig.getName() + "]");
    }
}
