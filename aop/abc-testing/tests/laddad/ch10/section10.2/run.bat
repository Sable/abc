del banking\*.class
del logging\*.class

call ajc banking\*.java logging\*.java
@echo on

call java banking.Test