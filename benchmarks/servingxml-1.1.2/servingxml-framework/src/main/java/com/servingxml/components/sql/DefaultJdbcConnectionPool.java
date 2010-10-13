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

package com.servingxml.components.sql;

import java.util.List;
import java.util.ArrayList;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

import com.servingxml.util.ServingXmlException;

/**
 * The <code>DefaultJdbcConnectionPool</code> implements a JDBC connection pool.
 *
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class DefaultJdbcConnectionPool implements JdbcConnectionPool {

  private final String driver;
  private final String databaseUrl;
  private final String user;
  private final String password;
  private final int minConnections;
  private final int maxConnections;
  private final String testStatement;
  private final SqlCommand[] commands;

  private List<Connection> connectionList = new ArrayList<Connection>();

  public DefaultJdbcConnectionPool(String driver, String databaseUrl, String user, String password,
  int minConnections, int maxConnections, String testStatement,
                                SqlCommand[] commands) throws SQLException {

    this.driver = driver;
    this.databaseUrl = databaseUrl;
    this.user = user;
    this.password = password;
    this.minConnections = minConnections;
    this.maxConnections = maxConnections;
    this.testStatement = testStatement;
    this.commands = commands;

    try {
      if (driver != null && driver.length() > 0) {
        //Object o = Class.forName(driver);
        //
        try {
          Thread.currentThread().getContextClassLoader().loadClass(driver).newInstance();
        } catch (InstantiationException e) {
          throw new ServingXmlException("Driver " + driver + " not resolved.");
        } catch (IllegalAccessException e) {
          throw new ServingXmlException("Driver " + driver + " not resolved.");
        }

      }

      synchronized(connectionList) {
        for (int i = 0; i < minConnections; ++i) {
          Connection connection = createConnection();
          connectionList.add(connection);
        }
      }
    } catch (java.lang.ClassNotFoundException e) {
      throw new SQLException("Failed to load jdbc driver class.  " + e.getMessage());
    }
  }

  public Connection getConnection() throws SQLException {
    Connection connection = null;

    synchronized(connectionList) {
      if (connectionList.size() > 0) {
          connection = connectionList.remove(connectionList.size()-1);
      }
      if (connection == null || !testConnection(connection)) {
        connection = createConnection();
      }
    }
    return connection;
  }

  private Connection createConnection() throws SQLException {

      Connection connection;
      if (user.length() > 0) {
        connection = DriverManager.getConnection(databaseUrl, user, password);
      } else {
        connection = DriverManager.getConnection(databaseUrl);
      }
      connection.setAutoCommit(false);
      if (!testConnection(connection)) {
        throw new SQLException("Bad connection");
      }
      for (int i = 0; i < commands.length; ++i) {
        commands[i].execute(connection);
      }
      return connection;
  }

  public void releaseConnection(Connection connection) {
    synchronized(connectionList) {
      if (connectionList.size() < maxConnections) {
        connectionList.add(connection);
      } else {
        try {
          connection.close();
        } catch(Exception e) {
        }
      }
    }
  }

  private boolean testConnection(Connection connection) {
    boolean test = true;

    Statement statement = null;
    try {
      if (connection.isClosed()) {
        test = false;
      } else if (testStatement != null && testStatement.length() > 0) {
        statement  = connection.createStatement();
        statement.execute(testStatement);
      }
    } catch (SQLException e) {
      test = false;
    } finally {
      try {
        statement.close();
      } catch (Exception e) {
      }
    }

    return test;
  }
}
