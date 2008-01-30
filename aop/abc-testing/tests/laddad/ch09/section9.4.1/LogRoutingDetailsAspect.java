//Listing 9.6 LogRoutingDetailsAspect.java

import pattern.worker.*;

public aspect LogRoutingDetailsAspect {
    pointcut syncRoutingExecution()
	: cflow(execution(* RunnableWithReturn.run()));

    before() : LogUIActivitiesAspect.uiActivities()
	&& syncRoutingExecution() {
	System.out.println("Executing operation synchronously");
    }
}
