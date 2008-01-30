del banking\*.class
del logging\*.class
del rule\common\*.class
del rule\java\*.class

call ajc banking\*.java logging\*.java rule\common\*.java rule\java\*.java
@echo on

call java banking.Test