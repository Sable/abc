//Listing 5.12 Tracing methods: the third version, using the log4j toolkit

import org.apache.log4j.*;
import org.aspectj.lang.*;

public aspect TraceAspectV3 {
    Logger _logger = Logger.getLogger("trace");

    TraceAspectV3() {
	_logger.setLevel(Level.ALL);
    }

    pointcut traceMethods()
	: (execution(* *.*(..))
	   || execution(*.new(..))) && !within(TraceAspectV3);

    before() : traceMethods() {
	if (_logger.isEnabledFor(Level.INFO)) {
	    Signature sig = thisJoinPointStaticPart.getSignature();
	    _logger.log(Level.INFO,
			"Entering ["
			+ sig.getDeclaringType().getName() + "."
			+ sig.getName() + "]");
	}
    }
}
