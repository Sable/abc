//Listing 5.13 Logging exception: the first version, using System.err as the logging stream

import org.aspectj.lang.*;

public aspect ExceptionLoggerAspectV1 {
    pointcut exceptionLogMethods()
	: call(* *.*(..)) &&
	!within(ExceptionLoggerAspectV1);

    after() throwing(Throwable ex) : exceptionLogMethods() {
	Signature sig = thisJoinPointStaticPart.getSignature();
	System.err.println("Exception logger aspect ["
			   + sig.getDeclaringType().getName() + "."
			   + sig.getName() + "]");
	ex.printStackTrace(System.err);
    }
}

