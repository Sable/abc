//Listing 11.12 Adding context creation to logging

package banking;

import java.sql.*;
import org.aspectj.lang.*;
import logging.*;
import transaction.jdbc.*;
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

    public pointcut aspectInstantiation()
	: execution(JDBCTransactionAspect+.new(..));

    public pointcut contextInstantiation()
	: execution(*.TransactionContext+.new(..));

    public pointcut loggedOperations()
	: accountActivities()
	|| connectionActivities(Connection)
	|| updateActivities(Statement)
	|| aspectInstantiation()
	|| contextInstantiation();

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

    before() : aspectInstantiation() || contextInstantiation() {
	Signature sig = thisJoinPointStaticPart.getSignature();
	System.out.println("[" + sig.getName() + "] "
			   + sig.getDeclaringType());
    }
}
