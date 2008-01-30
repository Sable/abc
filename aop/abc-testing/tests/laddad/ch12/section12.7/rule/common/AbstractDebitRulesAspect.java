//Listing 12.10 AbstractDebitRulesAspect.java: the base aspect

package rule.common;

import banking.*;

public abstract aspect AbstractDebitRulesAspect {
    public float Account.getAvailableBalance() {
	return getBalance();
    }

    public pointcut debitExecution(Account account,
				   float withdrawalAmount)
	: execution(void Account.debit(float)
		    throws InsufficientBalanceException)
	&& this(account) && args(withdrawalAmount);
}

