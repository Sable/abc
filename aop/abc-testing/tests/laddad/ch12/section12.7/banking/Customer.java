//Listing 12.2 Customer.java: a banking customer

package banking;

import java.util.*;

public class Customer {
    private String _name;
    private Collection _accounts = new Vector();
    private Collection _overdraftAccounts = new Vector();

    public Customer(String name) {
	_name = name;
    }

    public String getName() {
	return _name;
    }

    public void addAccount(Account account) {
	_accounts.add(account);
    }

    public Collection getAccounts() {
	return _accounts;
    }

    public void addOverdraftAccount(Account overdraftAccount) {
	_overdraftAccounts.add(overdraftAccount);
    }

    public Collection getOverdraftAccounts() {
	return _overdraftAccounts;
    }
}
