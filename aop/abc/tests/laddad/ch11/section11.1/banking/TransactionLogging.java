//Listing 11.4 The logging aspect

package banking;

import java.sql.*;
import org.aspectj.lang.*;
import logging.*;

public aspect TransactionLogging extends IndentedLogging {
    declare precedence: TransactionLogging, *;

    public pointcut accountActivities()
	: call(void Account.credit(..))
	|| call(void Account.debit(..))
	|| call(void InterAccountTransferSystem.transfer(..));

    public pointcut connectionActivities(Connection conn)
	: (call(* Connection.commit(..))
	   || call(* Connection.rollback(..)))
	&& target(conn);

    public pointcut updateActivities(Statement stmt)
	: call(* Statement.executeUpdate(..))
	&& target(stmt);

    public pointcut loggedOperations()
	: accountActivities()
	|| connectionActivities(Connection)
	|| updateActivities(Statement);

    before() : accountActivities() {
	Signature sig = thisJoinPointStaticPart.getSignature();
	System.out.println("[" + sig.getName() + "]");
    }

    before(Connection conn) : connectionActivities(conn) {
	Signature sig = thisJoinPointStaticPart.getSignature();
	System.out.println("[" + sig.getName() + "] " + conn);
    }

    before(Statement stmt) throws SQLException
	: updateActivities(stmt) {
	Signature sig = thisJoinPointStaticPart.getSignature();
	System.out.println("[" + sig.getName()
			   + "] " + stmt.getConnection());
    }
}
