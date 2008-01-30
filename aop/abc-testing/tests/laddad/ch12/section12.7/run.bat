del banking\*.class
del logging\*.class
del rule\jess\*.class

call ajc banking\*.java logging\*.java rule\common\*.java rule\jess\*.java
@echo on

call java banking.Test