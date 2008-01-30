//Listing 12.18 An aspect implementing rules using the Jess rule engine

package rule.jess;

import jess.*;
import banking.*;
import rule.common.*;

public aspect RuleEngineBasedDebitRulesAspect
    extends AbstractDebitRulesAspect {
    private static final float MINIMUM_BALANCE_REQD = 25;
    Rete _debitRete = new Rete();

    public RuleEngineBasedDebitRulesAspect() {
	try {
	    _debitRete.executeCommand(
			      "(batch rule/jess/debitRules.clp)");
	} catch (JessException ex) {
	    System.err.println(ex);
	}
    }

    public float SavingsAccount.getAvailableBalance() {
	return getBalance() - MINIMUM_BALANCE_REQD;
    }

    before(Account account, float withdrawalAmount)
	throws InsufficientBalanceException
	: debitExecution(account, withdrawalAmount) {
	invokeRules(account, withdrawalAmount, false);
    }

    pointcut checkClearanceTransaction()
	: execution(* CheckClearanceSystem.*(..));

    pointcut checkClearanceDebitExecution(Account account,
					  float withdrawalAmount)
	: debitExecution(account, withdrawalAmount)
	&& cflow(checkClearanceTransaction());

    before(Account account, float withdrawalAmount)
	throws InsufficientBalanceException
	: checkClearanceDebitExecution(account, withdrawalAmount) {
	invokeRules(account, withdrawalAmount, true);
    }

    private void invokeRules(Account account,
			     float withdrawalAmount,
			     boolean isCheckClearance)
	throws InsufficientBalanceException {
	try {
	    _debitRete.store("checkClearanceTransaction",
			     new Value(isCheckClearance));
	    _debitRete.store("current-account", account);
	    _debitRete.store("transaction-amount",
			     new Value(withdrawalAmount,
				       RU.INTEGER));
	    _debitRete.reset();
	    _debitRete.run();
	} catch (JessException ex) {
	    Throwable originalException = ex.getNextException();
	    if (originalException
		instanceof InsufficientBalanceException) {
		throw
		    (InsufficientBalanceException)originalException;
	    }
	    System.err.println(ex);
	}
    }
}
