del *.class

call ajc RemoteService.java RemoteClient.java FailureHandlingAspect.java
@echo on
call java RemoteClient