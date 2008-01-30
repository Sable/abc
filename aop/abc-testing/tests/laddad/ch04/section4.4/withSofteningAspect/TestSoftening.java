//Listing 4.20 TestSoftening.java: code for testing the effect of softening an exception

import java.rmi.RemoteException;

public class TestSoftening {
    public static void main(String[] args) {
	TestSoftening test = new TestSoftening();
	test.perform();
    }

    public void perform() throws RemoteException {
	throw new RemoteException();
    }
}
