//Listing 7.3 DBConnectionPool.java

import java.sql.*;

public interface DBConnectionPool {
    public Connection getConnection(String url, String userName,
				    String password)
	throws SQLException;
    public boolean putConnection(Connection connection);
    public void registerConnection(Connection connection,
				   String url, String userName, 
				   String password);
}
