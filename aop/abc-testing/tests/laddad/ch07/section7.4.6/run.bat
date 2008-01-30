del *.class

call ajc -source 1.4 *.java
@echo on

call start java -ea EchoServer 10000
@echo -------------------------------------------------------------------------
@echo Type in a few strings and then "quit".
@echo -------------------------------------------------------------------------
call java -ea EchoClient localhost 10000
@echo -------------------------------------------------------------------------
@echo Enter a few strings and then "quit"
@echo -------------------------------------------------------------------------

call java -ea EchoClient localhost 10000
@echo on