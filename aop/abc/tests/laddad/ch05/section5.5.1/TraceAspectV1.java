//Listing 5.17 TraceAspectV1.java: modified to log method parameters

import org.aspectj.lang.*;

public aspect TraceAspectV1 {
    pointcut traceMethods()
	: (execution(* *.*(..))
	   || execution(*.new(..))) && !within(TraceAspectV1);

    before() : traceMethods()&& !execution(String *.toString()){
	Signature sig = thisJoinPointStaticPart.getSignature();
	System.err.println("Entering ["
			   + sig.getDeclaringType().getName() + "."
			   + sig.getName() + "]"
			   + createParameterMessage(thisJoinPoint));
    }

    private String createParameterMessage(JoinPoint joinPoint) {
	StringBuffer paramBuffer = new StringBuffer("\n\t[This: ");
	paramBuffer.append(joinPoint.getThis());
	Object[] arguments = joinPoint.getArgs();
	paramBuffer.append("]\n\t[Args: (");
	for (int length = arguments.length, i = 0; i < length; ++i) {
	    Object argument = arguments[i];
	    paramBuffer.append(argument);
	    if (i != length-1) {
		paramBuffer.append(',');
	    }
	}
	paramBuffer.append(")]");
	return paramBuffer.toString();
    }
}

