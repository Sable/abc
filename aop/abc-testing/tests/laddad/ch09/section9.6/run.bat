del *.class

call ajc TestResponsiveness.java
@echo ***** Running without aspects *****
@echo on
call java TestResponsiveness

del *.class
call ajc *.java
@echo ***** Running with aspects *****
@echo on
call java TestResponsiveness