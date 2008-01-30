//Listing 10.12 AccountSimpleImpl.java: the conventional way

package banking;

import java.security.AccessController;

public class AccountSimpleImpl implements Account {
    private int _accountNumber;
    private float _balance;

    public AccountSimpleImpl(int accountNumber) {
	_accountNumber = accountNumber;
    }

    public int getAccountNumber() {
	AccessController.checkPermission(
			  new BankingPermission("getAccountNumber"));
	return _accountNumber;
    }

    public void credit(float amount) {
	AccessController.checkPermission(
			 new BankingPermission("credit"));
	_balance = _balance + amount;
    }

    public void debit(float amount)
	throws InsufficientBalanceException {
	AccessController.checkPermission(
			 new BankingPermission("debit"));
	if (_balance < amount) {
	    throw new InsufficientBalanceException(
			   "Total balance not sufficient");
	} else {
	    _balance = _balance - amount;
	}
    }

    public float getBalance() {
	AccessController.checkPermission(
			 new BankingPermission("getBalance"));
	return _balance;	
    }
}
