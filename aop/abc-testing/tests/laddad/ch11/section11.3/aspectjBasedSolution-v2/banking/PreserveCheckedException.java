//Listing 11.8 An aspect that preserves the checked exception

package banking;

import transaction.jdbc.JDBCTransactionAspect;

public aspect PreserveCheckedException {
    after() throwing(JDBCTransactionAspect.TransactionException ex)
	throws InsufficientBalanceException
	: call(* banking..*.*(..)
	       throws InsufficientBalanceException) {
	Throwable cause = ex.getCause();
	if (cause instanceof InsufficientBalanceException) {
	    throw (InsufficientBalanceException)cause;
	    
	} else {
	    throw ex;
	}
    }
}
