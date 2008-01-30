//Listing 12.7 SavingsAccountSimpleImpl.java

package banking;

public class SavingsAccountSimpleImpl
    extends AccountSimpleImpl implements SavingsAccount {
    public SavingsAccountSimpleImpl(int accountNumber,
				    Customer customer) {
	super(accountNumber, customer);
    }

    public String toString() {
	return "SavingsAccount(" + getAccountNumber() + ")";
    }
}
