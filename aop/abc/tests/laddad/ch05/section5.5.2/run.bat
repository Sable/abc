del *.class
del logging\*.class

call ajc *.java logging\IndentedLogging.java
@echo on

call java Test