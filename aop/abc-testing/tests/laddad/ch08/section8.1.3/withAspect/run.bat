del *.class

call ajc CachePreFetcher.java ProjectSaver.java Test.java AsynchronousExecutionAspect.java SystemAsynchronousExecutionAspect.java
@echo on

call java Test