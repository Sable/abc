// Listing 3.3 FailureHandlingAspect.java

import java.rmi.RemoteException;

public aspect FailureHandlingAspect {
    final int MAX_RETRIES = 3;

    Object around() throws RemoteException
	: call(* RemoteService.get*(..) throws RemoteException) {
	int retry = 0;
	while(true){
	    try{
		return proceed();
	    } catch(RemoteException ex){
		System.out.println("Encountered " + ex);
		if (++retry > MAX_RETRIES) {
		    throw ex;
		}
		System.out.println("\tRetrying...");
	    }
	}
    }
}

