//Listing 2.9 JoinPointTraceAspect.java

public aspect JoinPointTraceAspect {
    private int _callDepth = -1;

    pointcut tracePoints() : !within(JoinPointTraceAspect);

    before() : tracePoints() {
	_callDepth++;
	print("Before", thisJoinPoint);
    }

    after() : tracePoints() {
	print("After", thisJoinPoint);
	_callDepth--;
    }

    private void print(String prefix, Object message) {
	for(int i = 0, spaces = _callDepth * 2; i < spaces; i++) {
	    System.out.print(" ");
	}
	System.out.println(prefix + ": " + message);
    }
}

