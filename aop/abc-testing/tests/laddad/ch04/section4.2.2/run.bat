cls
del *.class

call ajc Home.java TestHome.java HomeSecurityAspect.java SaveEnergyAspect.java HomeSystemCoordinationAspect.java
@echo on
call java TestHome