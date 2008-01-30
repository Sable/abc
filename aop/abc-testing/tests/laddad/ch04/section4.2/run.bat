cls
del *.class

call ajc Home.java TestHome.java HomeSecurityAspect.java SaveEnergyAspect.java
@echo on
call java TestHome