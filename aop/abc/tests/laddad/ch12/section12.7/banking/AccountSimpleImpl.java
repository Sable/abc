//Listing 12.6 AccountSimpleImpl.java: the base account implementation

package banking;

public abstract class AccountSimpleImpl implements Account {
    private int _accountNumber;
    private float _balance;
    private Customer _customer;

    public AccountSimpleImpl(int accountNumber, Customer customer) {
	_accountNumber = accountNumber;
	_customer = customer;
    }

    public int getAccountNumber() {
	return _accountNumber;
    }

    public void credit(float amount) {
	_balance = _balance + amount;
    }

    public void debit(float amount)
	throws InsufficientBalanceException {
	if (_balance < amount) {
	    throw new InsufficientBalanceException(
				   "Total balance not sufficient");
	} else {
	    _balance = _balance - amount;
	}
    }

    public float getBalance() {
	return _balance;
    }
    
    public Customer getCustomer() {
	return _customer;
    }
}
