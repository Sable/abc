//Listing 11.1 DatabaseHelper.java

package banking;

import java.sql.*;

public class DatabaseHelper {
    static {
	try {
	    Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
	} catch (Exception ex) {
	    // ignore...
	}
    }

    public static Connection getConnection() throws SQLException {
	String url = "jdbc:odbc:bank";
	String user = "user1";
	String password = "password1";
	Connection connection
	    = DriverManager.getConnection(url, user, password);
	connection.setAutoCommit(true);
	return connection;
    }
}
