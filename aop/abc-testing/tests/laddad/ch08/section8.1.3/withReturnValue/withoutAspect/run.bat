del *.class

@echo ****** Compiling and running without aspects ******
call ajc TestSynchronous.java
@echo on

call java TestSynchronous