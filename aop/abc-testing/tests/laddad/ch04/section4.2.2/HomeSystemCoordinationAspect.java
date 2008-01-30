//Listing 4.7 HomeSystemCoordinationAspect.java

public aspect HomeSystemCoordinationAspect {
    declare precedence: HomeSecurityAspect, SaveEnergyAspect;
}
