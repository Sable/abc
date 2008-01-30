//Listing 5.11 Tracing methods: the second version, using the standard logging toolkit

import java.util.logging.*;
import org.aspectj.lang.*;

public aspect TraceAspectV2 {
    private Logger _logger = Logger.getLogger("trace");

    TraceAspectV2() {
	_logger.setLevel(Level.ALL);
    }

    pointcut traceMethods()
	: (execution(* *.*(..))
	   || execution(*.new(..))) && !within(TraceAspectV2);

    before() : traceMethods() {
	if (_logger.isLoggable(Level.INFO)) {
	    Signature sig = thisJoinPointStaticPart.getSignature();
	    _logger.logp(Level.INFO,
			 sig.getDeclaringType().getName(),
			 sig.getName(),
			 "Entering");
	}
    }
}

