//Listing 11.10 The participant aspect nested in the InterAccountTransferSystem class

package banking;

import transaction.jdbc.JDBCTransactionAspect;

public class InterAccountTransferSystem {
    public static void transfer(Account from, Account to,
				float amount)
	throws InsufficientBalanceException {
	to.credit(amount);
	from.debit(amount);
    }

    public static aspect TransactionParticipantAspect
	extends JDBCTransactionAspect {
	protected pointcut transactedOperation()
	    : execution(* InterAccountTransferSystem.transfer(..));
	protected pointcut obtainConnection();
    }
}

