//Listing 10.4 InterAccountTransferSystem.java

package banking;

public class InterAccountTransferSystem {
    public static void transfer(Account from, Account to,
				float amount)
	throws InsufficientBalanceException {
	to.credit(amount);
	from.debit(amount);
    }
}

