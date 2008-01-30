//Listing 7.6 SimpleDBConnectionPool.java

import java.sql.*;
import java.util.*;

public class SimpleDBConnectionPool implements DBConnectionPool {
    List _pooledConnections = new ArrayList();

    Map _connectionDescriptionMap = new HashMap();

    synchronized
	public Connection getConnection(String url, String userName,
					String password)
	throws SQLException {
	DBConnectionDescription desc
	    = new DBConnectionDescription(url, userName, password);
	List connectionsList = getConnections(desc);
	if (connectionsList == null) {
	    return null;
	}
	for (int size = _pooledConnections.size(), i = 0; i < size; ++i) {
	    Connection connection = (Connection)_pooledConnections.get(i);
	    if (connectionsList.contains(connection)) {
		_pooledConnections.remove(connection);
		if (!connection.isClosed()) {
		    return connection;
		}
	    }
	}
	return null;
    }

    synchronized
	public boolean putConnection(Connection connection) {
	_pooledConnections.add(connection);
	return true;
    }
    
    public void registerConnection(Connection connection,
				   String url, String userName,
				   String password) {
	DBConnectionDescription desc
	    = new DBConnectionDescription(url, userName, password);
	List connectionsList = getConnections(desc);
	if (connectionsList == null) {
	    connectionsList = new ArrayList();
	    _connectionDescriptionMap.put(desc, connectionsList);
	}
	connectionsList.add(connection);
    }

    private List getConnections(DBConnectionDescription desc) {
	return (List)_connectionDescriptionMap.get(desc);
    }
}
