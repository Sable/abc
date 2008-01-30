del *.class

@echo ****** Compiling and running with aspects ******
call ajc TestSynchronous.java SynchronousExecutionAspect.java SystemSynchronousExecutionAspect.java pattern\worker\RunnableWithReturn.java
@echo on

call java TestSynchronous
