//Listing 3.5 OptimizeFactorialAspect.java: aspect for caching results

import java.util.*;

public aspect OptimizeFactorialAspect {
    pointcut factorialOperation(int n) :
	call(long *.factorial(int)) && args(n);

    pointcut topLevelFactorialOperation(int n) :
	factorialOperation(n)
	&& !cflowbelow(factorialOperation(int));

    private Map _factorialCache = new HashMap();

    before(int n) : topLevelFactorialOperation(n) {
	System.out.println("Seeking factorial for " + n);
    }

    long around(int n) : factorialOperation(n) {
	Object cachedValue = _factorialCache.get(new Integer(n));
	if (cachedValue != null) {
	    System.out.println("Found cached value for " + n
			       + ": " + cachedValue);
	    return ((Long)cachedValue).longValue();
	}
	return proceed(n);
    }

    after(int n) returning(long result)
	: topLevelFactorialOperation(n) {
	_factorialCache.put(new Integer(n), new Long(result));
    }
}

