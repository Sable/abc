//Listing 11.5 JDBCTransactionAspect.java

package transaction.jdbc;

import java.sql.*;

public abstract aspect JDBCTransactionAspect
    percflow(topLevelTransactedOperation()) {
    private Connection _connection;
    protected abstract pointcut transactedOperation();
    protected abstract pointcut obtainConnection();

    protected pointcut topLevelTransactedOperation()
	: transactedOperation()
	&& !cflowbelow(transactedOperation());

    Object around() : topLevelTransactedOperation() {
	Object operationResult;
	try {
	    operationResult = proceed();
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
	return operationResult;
    }

    Connection around() throws SQLException
	: obtainConnection() && cflow(transactedOperation()) {
	if (_connection == null) {
	    _connection = proceed();
	    _connection.setAutoCommit(false);
	}
	return _connection;
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
