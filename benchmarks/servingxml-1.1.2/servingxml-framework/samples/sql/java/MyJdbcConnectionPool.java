/**
 *  ServingXML
 *  
 *  Copyright (C) 2006  Daniel Parker
 *    daniel.parker@servingxml.com 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import com.servingxml.components.sql.JdbcConnectionPool;
import com.servingxml.util.ServingXmlException;

/**
 * The <code>MyJdbcConnectionPool</code> class implements a simple 
 * custom JDBC "connection pool" that creates a new connection every time.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class MyJdbcConnectionPool implements JdbcConnectionPool {

  private final String driver;
  private final String databaseUrl;
  private final String user;
  private final String password;

  public MyJdbcConnectionPool(Properties properties) {
    this.driver = properties.getProperty("driver");
    this.databaseUrl = properties.getProperty("databaseUrl");
    this.user = properties.getProperty("user");
    this.password = properties.getProperty("password");

    if (driver == null) {
      throw new ServingXmlException("driver required");
    }

    try {
      Thread.currentThread().getContextClassLoader().loadClass(driver);
    } catch (java.lang.ClassNotFoundException e) {
      throw new ServingXmlException("Failed to load jdbc driver class.  " + e.getMessage());
    }

    if (databaseUrl == null) {
      throw new ServingXmlException("databaseUrl required");
    }
    if (user == null) {
      throw new ServingXmlException("user required");
    }
    if (password == null) {
      throw new ServingXmlException("password required");
    }
  }

  public Connection getConnection() throws SQLException {
    Connection connection = DriverManager.getConnection(databaseUrl, user, password);
    return connection;
  }

  public void releaseConnection(Connection connection) {
    try {
      connection.close();
    } catch(Exception e) {
    }
  }
}
