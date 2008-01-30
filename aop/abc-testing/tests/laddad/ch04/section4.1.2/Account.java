//Listing 2.5 Account.java

public abstract class Account {
    private float _balance;
    private int _accountNumber;

    public Account(int accountNumber) {
	_accountNumber = accountNumber;
    }

    public void credit(float amount) {
	setBalance(getBalance() + amount);
    }

    public void debit(float amount)
	throws InsufficientBalanceException {
	float balance = getBalance();
	if (balance < amount) {
	    throw new InsufficientBalanceException(
			   "Total balance not sufficient");
	} else {
	    setBalance(balance - amount);
	}
    }

    public float getBalance() {
	return _balance;
    }

    public void setBalance(float balance) {
	_balance = balance;
    }
}
