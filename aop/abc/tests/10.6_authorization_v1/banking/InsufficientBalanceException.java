//Listing 10.2 InsufficientBalanceException.java

package banking;

public class InsufficientBalanceException extends Exception {
    public InsufficientBalanceException(String message) {
	super(message);
    }
}
