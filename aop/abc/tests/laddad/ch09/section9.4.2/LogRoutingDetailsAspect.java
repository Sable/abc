//Listing 9.9 LogRoutingDetailsAspect

import pattern.worker.*;

public aspect LogRoutingDetailsAspect {
    pointcut syncRoutingExecution()
	: cflow(execution(* RunnableWithReturn.run()));

    before() : LogUIActivitiesAspect.uiActivities()
	&& syncRoutingExecution() {
	System.out.println("Executing operation synchronously");
    }

    pointcut asyncRoutingExecution()
	: cflow(execution(* Runnable.run()))
	&& !syncRoutingExecution();

    before() : LogUIActivitiesAspect.uiActivities()
	&& asyncRoutingExecution() {
	System.out.println("Executing operation asynchronously");
    }
}
