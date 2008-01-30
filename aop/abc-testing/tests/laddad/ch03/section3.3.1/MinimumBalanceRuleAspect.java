//Listing 3.6 MinimumBalanceRuleAspect.java

public aspect MinimumBalanceRuleAspect {
    private float Account._minimumBalance;

    public float Account.getAvailableBalance() {
	return getBalance() - _minimumBalance;
    }

    after(Account account) :
	execution(SavingsAccount.new(..)) && this(account) {
	account._minimumBalance = 25;
    }

    before(Account account, float amount)
	throws InsufficientBalanceException :
	execution(* Account.debit())
	&& this(account) && args(amount) {
	if (account.getAvailableBalance() < amount) {
	    throw new InsufficientBalanceException(
			   "Insufficient available balance");
	}
    }
}
