//Listing 3.1 RemoteService.java

import java.rmi.RemoteException;

public class RemoteService {
    public static int getReply() throws RemoteException {
	if(Math.random() > 0.25) {
	    throw new RemoteException("Simulated failure occurred");
	}
	System.out.println("Replying");
	return 5;
    }
}
