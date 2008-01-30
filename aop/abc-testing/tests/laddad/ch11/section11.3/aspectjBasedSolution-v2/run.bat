del banking\*.class
del logging\*.class
del transaction\jdbc\*.class

call ajc banking\*.java logging\*.java transaction\jdbc\*.java
@echo on

call java banking.Test