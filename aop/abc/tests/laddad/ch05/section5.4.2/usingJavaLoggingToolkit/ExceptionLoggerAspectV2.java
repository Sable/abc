//Listing 5.15 Logging exception: second version, using Java’s standard logging toolkit

import java.util.logging.*;
import org.aspectj.lang.*;

public aspect ExceptionLoggerAspectV2 {
    Logger _logger = Logger.getLogger("exceptions");

    ExceptionLoggerAspectV2() {
	_logger.setLevel(Level.ALL);
    }

    pointcut exceptionLogMethods()
	: call(* *.*(..)) && !within(ExceptionLoggerAspectV2);

    after() throwing(Throwable ex) : exceptionLogMethods() {
	if (_logger.isLoggable(Level.WARNING)) {
	    Signature sig = thisJoinPointStaticPart.getSignature();
	    _logger.logp(Level.WARNING,
			 sig.getDeclaringType().getName(),
			 sig.getName(),
			 "Exception logger aspect", ex);
	}
    }
}
