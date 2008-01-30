//Listing 8.5 SystemAsynchronousExecutionAspect.java

public aspect SystemAsynchronousExecutionAspect
    extends AsynchronousExecutionAspect{
    public pointcut asyncOperations()
	: call(* CachePreFetcher.fetch())
	|| call(* ProjectSaver.backupSave())
	/* || ... */;
}
