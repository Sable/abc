//Listing 8.8 SystemSynchronousExecutionAspect.java

import java.util.Vector;

public aspect SystemSynchronousExecutionAspect
    extends SynchronousExecutionAspect{
    public pointcut syncOperations()
	: (call(* Math.max(..))
	   || call(* Vector.*(..))
	   /* || ... */);
}
