call javac shopping\*.java
call jar -cvfm shopping.jar shopping.mf shopping\*.class
call ajc -injars shopping.jar tracing\TraceAspect.java -outjar tracedShopping.jar
@echo on
call java -classpath %CLASSPATH%;tracedShopping.jar Test
