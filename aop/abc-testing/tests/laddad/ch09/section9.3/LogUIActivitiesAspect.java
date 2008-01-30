//Listing 9.2 LogUIActivitiesAspect.java

import java.awt.EventQueue;

public aspect LogUIActivitiesAspect {
    pointcut uiActivities()
	: call(* javax..*+.*(..));

    before() : uiActivities() {
	System.out.println("Executing:\n\t"
			   + thisJoinPointStaticPart.getSignature()
			   + "\n\t"
			   + Thread.currentThread() + "\n");
    }
}
