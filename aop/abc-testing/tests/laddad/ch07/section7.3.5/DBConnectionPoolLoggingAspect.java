//Listing 7.8 DBConnectionPoolLoggingAspect.java

import java.sql.*;

public aspect DBConnectionPoolLoggingAspect {
    declare precedence: *, DBConnectionPoolLoggingAspect;

    after(String url, String userName, String password)
	returning(Connection connection)
	: call(Connection DBConnectionPool.getConnection(..))
	&& args(url, userName, password) {
	System.out.println("For [" + url + "," + userName
			   + "," + password + "]"
			   + "\n\tGot from pool: " + connection);
    }

    after(String url, String userName, String password)
	returning(Connection connection)
	: call(Connection DriverManager.getConnection(..))
	&& args(url, userName, password) {
	System.out.println("For [" + url + "," + userName
			   + "," + password + "]"
			   + "\n\tCreated new : " + connection);
    }

    before(Connection connection)
	: call(* DBConnectionPool.putConnection(Connection))
	&& args(connection) {
	System.out.println("Putting in pool: " + connection + "\n");
    }

    before(Connection connection)
	: call(* Connection.close())
	&& target(connection) {
	System.out.println("Closing: " + connection + "\n");
    }
}

