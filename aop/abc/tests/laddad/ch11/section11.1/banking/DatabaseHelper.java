//Listing 11.1 DatabaseHelper.java

package banking;

import java.sql.*;

public class DatabaseHelper {
    static {
	try {
	    Class.forName("com.mysql.jdbc.Driver");
	} catch (Exception ex) {
	    // ignore...
	}
    }

    public static Connection getConnection() throws SQLException {
	String url = "jdbc:mysql:///laddad";
	String user = "laddad";
	String password = "laddad";
	Connection connection
	    = DriverManager.getConnection(url, user, password);
	connection.setAutoCommit(true);
	return connection;
    }
}
