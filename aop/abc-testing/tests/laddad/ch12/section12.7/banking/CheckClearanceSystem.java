//Listing 12.9 A class implementing the check clearance system

package banking;

public class CheckClearanceSystem {
    public static void debit(Account account, float amount)
	throws InsufficientBalanceException {
	account.debit(amount);
    }

    public static void credit(Account account, float amount) {
	account.credit(amount);
    }
}
