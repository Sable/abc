cls
del *.class

call ajc TestSoftening.java SofteningTestAspect.java
@echo on

call java TestSoftening
