del banking\*.class
del logging\*.class
del transaction\jdbc\*.class
del pattern\worker\*.class

call ajc banking\*.java logging\*.java transaction\jdbc\*.java pattern\worker\*.java

@echo on

call java banking.Test