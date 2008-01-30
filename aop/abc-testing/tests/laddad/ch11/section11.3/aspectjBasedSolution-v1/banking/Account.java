//Listing 10.1 Account.java

package banking;

public interface Account {
    public int getAccountNumber();
    public void credit(float amount);
    public void debit(float amount)
	throws InsufficientBalanceException;
    public float getBalance();
}
