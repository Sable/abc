//Listing 11.6 BankingTransactionAspect.java

package banking;

import java.sql.Connection;
import transaction.jdbc.JDBCTransactionAspect;

public aspect BankingTransactionAspect
    extends JDBCTransactionAspect {
    protected pointcut transactedOperation()
	: execution(* AccountJDBCImpl.debit(..))
	|| execution(* AccountJDBCImpl.credit(..))
	|| execution(* InterAccountTransferSystem.transfer(..));

    protected pointcut obtainConnection()
	: call(Connection DatabaseHelper.getConnection(..));
}
