//Listing 5.16 Logging exception: third version, using the log4j toolkit

import org.apache.log4j.*;
import org.aspectj.lang.*;

public aspect ExceptionLoggerAspectV3 {
    Logger _logger = Logger.getLogger("exceptions");

    ExceptionLoggerAspectV3() {
	_logger.setLevel(Level.ALL);
    }

    pointcut exceptionLogMethods()
	: call(* *.*(..)) && !within(ExceptionLoggerAspectV3);

    after() throwing(Throwable ex) : exceptionLogMethods() {
	if (_logger.isEnabledFor(Level.ERROR)) {
	    Signature sig = thisJoinPointStaticPart.getSignature();
	    _logger.log(Level.ERROR,
			"Exception logger aspect ["
			+ sig.getDeclaringType().getName() + "."
			+ sig.getName() + "]", ex);
	}
    }
}
