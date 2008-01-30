//Listing 12.8 CheckingAccountSimpleImpl.java

package banking;

public class CheckingAccountSimpleImpl
    extends AccountSimpleImpl implements CheckingAccount {
    public CheckingAccountSimpleImpl(int accountNumber,
				     Customer customer) {
	super(accountNumber, customer);
    }
    
    public String toString() {
	return "CheckingAccount(" + getAccountNumber() + ")";
    }
}
