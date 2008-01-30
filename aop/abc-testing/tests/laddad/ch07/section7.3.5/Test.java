//Listing 7.7 Test.java

import java.sql.*;

public class Test {
    public static void main(String[] args) throws Exception {
	Class.forName("com.mysql.jdbc.Driver");
	printTable("jdbc:mysql:///laddad", "price",
		   "laddad", "laddad");
	printTable("jdbc:mysql:///laddad", "price",
		   "laddad", "laddad");
	printTable("jdbc:mysql:///laddad", "price",
		   "laddad", "laddad");
	printTable("jdbc:mysql:///laddad", "price",
		   "laddad", "laddad");
    }

    static void printTable(String url, String table,
			   String user, String password)
	throws SQLException {
	Connection connection
	    = DriverManager.getConnection(url, user, password);
	Statement stmt = connection.createStatement();
	ResultSet rs = stmt.executeQuery("select * from " + table);
	ResultSetMetaData rsmd = rs.getMetaData();
	int numCols = rsmd.getColumnCount();
	while (rs.next()) {
	    for (int i = 1; i < numCols+1; ++i) {
		System.out.print(rs.getString(i) + "\t");
	    }
	    System.out.println();
	}
	rs.close();
	connection.close();
    }
}
