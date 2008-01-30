//Listing 11.2 AccountJDBCImpl.java

package banking;

import java.sql.*;

public class AccountJDBCImpl implements Account {
    private int _accountNumber;

    public AccountJDBCImpl(int accountNumber) {
	_accountNumber = accountNumber;
    }

    public int getAccountNumber() {
	return _accountNumber;
    }

    public void credit(float amount) {
	float updatedBalance = getBalance() + amount;
	setBalance(updatedBalance);
    }

    public void debit(float amount)
	throws InsufficientBalanceException {
	float balance = getBalance();
	if (balance < amount) {
	    throw new InsufficientBalanceException(
				   "Total balance not sufficient");
	} else {
	    float updatedBalance = balance - amount;
	    setBalance(updatedBalance);
	}
    }

    public float getBalance() {
	Connection conn = DatabaseHelper.getConnection();
	Statement stmt = conn.createStatement();
	ResultSet rs
	    = stmt.executeQuery("select balance from accounts "
				+ "where accountNumber = "
				+ _accountNumber);
	rs.next();
	float balance = rs.getFloat(1);
	stmt.close();
	conn.close();
	return balance;
    }

    private void setBalance(float balance) throws SQLException {
	Connection conn = DatabaseHelper.getConnection();
	Statement stmt = conn.createStatement();
	stmt.executeUpdate("update accounts set balance = "
			   + balance +
			   " where accountNumber = "
			   + _accountNumber);
	stmt.close();
	conn.close();
    }
    
    private static aspect SoftenSQLException {
	declare soft : SQLException
	    : execution(* Account.*(..))
	    && within(AccountJDBCImpl);
    }
}
