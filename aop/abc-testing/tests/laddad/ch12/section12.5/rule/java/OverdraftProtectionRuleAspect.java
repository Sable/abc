//Listing 12.12 An aspect that implements the overdraft protection rule

package rule.java;

import java.util.*;
import banking.*;
import rule.common.*;

public aspect OverdraftProtectionRuleAspect
    extends AbstractDebitRulesAspect {
    pointcut checkClearanceTransaction()
	: execution(* CheckClearanceSystem.*(..));

    pointcut checkingDebitExecution(Account account,
				    float withdrawalAmount)
	: debitExecution(account, withdrawalAmount)
	&& this(CheckingAccount);

    before(Account account, float withdrawalAmount)
	throws InsufficientBalanceException
	: checkingDebitExecution(account, withdrawalAmount)
	&& cflow(checkClearanceTransaction()) {
	if (account.getAvailableBalance() < withdrawalAmount) {
	    performOverdraftProtection(account, withdrawalAmount);
	}
    }

    private void performOverdraftProtection(Account account,
					    float withdrawalAmount)
	throws InsufficientBalanceException {
	float transferAmountNeeded
	    = withdrawalAmount - account.getAvailableBalance();
	Customer customer = account.getCustomer();
	Collection overdraftAccounts
	    = customer.getOverdraftAccounts();
	for (Iterator iter = overdraftAccounts.iterator();
	     iter.hasNext();) {
	    Account overdraftAccount = (Account)iter.next();
	    if (overdraftAccount == account) {
		continue;
	    }
	    if (transferAmountNeeded <
		overdraftAccount.getAvailableBalance()) {
		overdraftAccount.debit(transferAmountNeeded);
		account.credit(transferAmountNeeded);
		return;
	    }
	}
	throw new InsufficientBalanceException(
			       "Insufficient funds in overdraft accounts");
    }
}
