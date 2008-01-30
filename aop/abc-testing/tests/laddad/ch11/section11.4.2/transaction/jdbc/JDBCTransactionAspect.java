//Listing 11.11 JDBCTransactionAspect.java: the improved version

package transaction.jdbc;

import java.sql.*;
import pattern.worker.*;

public abstract aspect JDBCTransactionAspect {
    protected abstract pointcut transactedOperation();

    protected abstract pointcut obtainConnection();

    protected pointcut
	inTransactedOperation(TransactionContext context)
	: cflow(execution(* TransactionContext.run())
		&& this(context));

    Object around() : transactedOperation()
	&& !inTransactedOperation(TransactionContext) {
	TransactionContext transactionContext
	    = new TransactionContext() {
		    public void run() {
			try {
			    _returnValue = proceed();
			    if (_connection != null) {
				_connection.commit();
			    }
			} catch (Exception ex) {
			    if (_connection != null) {
				_connection.rollback();
			    }
			    throw new TransactionException(ex);
			} finally {
			    if (_connection != null) {
				_connection.close();
			    }
			}
		    }};
	transactionContext.run();
	return transactionContext.getReturnValue();
    }

    Connection around(final TransactionContext context)
	throws SQLException
	: obtainConnection() && inTransactedOperation(context) {
	if (context._connection == null) {
	    context._connection = proceed(context);
	    context._connection.setAutoCommit(false);
	}
	return context._connection;
    }

    public static abstract class TransactionContext
	extends RunnableWithReturn {
	Connection _connection;
    }

    public static class TransactionException
	extends RuntimeException {
	public TransactionException(Exception cause) {
	    super(cause);
	}
    }

    private static aspect SoftenSQLException {
	declare soft : java.sql.SQLException
	    : (call(void Connection.rollback())
	       || call(void Connection.close()))
	    && within(JDBCTransactionAspect);
    }

    pointcut illegalConnectionManagement()
	: (call(void Connection.close())
	   || call(void Connection.commit())
	   || call(void Connection.rollback())
	   || call(void Connection.setAutoCommit(boolean)))
	&& !within(JDBCTransactionAspect);

    void around() : illegalConnectionManagement() {
	// Don't call proceed(); we want to bypass
	// illegal connection management here
    }
}
