//Listing 9.7 PrecedenceControlAspect.java

public aspect PrecedenceControlAspect {
    declare precedence:
	DefaultSwingThreadSafetyAspect,
	LogRoutingDetailsAspect,
	LogUIActivitiesAspect;
}
