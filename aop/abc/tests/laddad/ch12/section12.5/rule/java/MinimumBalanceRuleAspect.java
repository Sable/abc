//Listing 12.11 An aspect that implements the rule for enforcing a minimum balance

package rule.java;

import rule.common.*;
import banking.*;

public aspect MinimumBalanceRuleAspect
    extends AbstractDebitRulesAspect {
    private static final float MINIMUM_BALANCE_REQD = 25;

    public float SavingsAccount.getAvailableBalance() {
	return getBalance() - MINIMUM_BALANCE_REQD;
    }

    pointcut savingsDebitExecution(Account account,
				   float withdrawalAmount)
	: debitExecution(account, withdrawalAmount)
	&& this(SavingsAccount);

    before(Account account, float withdrawalAmount)
	throws InsufficientBalanceException
	: savingsDebitExecution(account, withdrawalAmount) {
	if (account.getAvailableBalance() < withdrawalAmount) {
	    throw new InsufficientBalanceException(
			   "Minimum balance condition not met");
	}
    }
}
