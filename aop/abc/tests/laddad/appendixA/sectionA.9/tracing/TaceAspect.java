//Listing 5.9 TraceAspect performing the same job

import java.util.logging.*;
import org.aspectj.lang.*;

public aspect TaceAspect {
    private Logger _logger = Logger.getLogger("trace");

    pointcut traceMethods()
	: execution(* *.*(..)) && !within(TraceAspect);
    
    before() : traceMethods() {
	Signature sig = thisJoinPointStaticPart.getSignature();
	_logger.logp(Level.INFO, sig.getDeclaringType().getName(),
		     sig.getName(), "Entering");
    }
}
