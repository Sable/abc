del shopping.jar

call javac shopping\*.java
call jar -cf shopping.jar shopping\*.class
call ajc -injars shopping.jar tracing\TraceAspect.java
@echo on
call java -cp shopping;tracing;%CLASSPATH% Test
