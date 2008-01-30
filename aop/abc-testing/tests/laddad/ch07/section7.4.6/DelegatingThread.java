//Listing 7.12 DelegatingThread.java

public class DelegatingThread extends Thread {
    private Runnable _delegatee;

    public void setDelegatee(Runnable delegatee) {
	_delegatee = delegatee;
    }

    public void run() {
	_delegatee.run();
    }
}
