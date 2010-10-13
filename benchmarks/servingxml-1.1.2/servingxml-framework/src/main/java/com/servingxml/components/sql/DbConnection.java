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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.servingxml.util.ServingXmlException;


/**
 * The <code>DbConnection</code> class encapsulates a database connection.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class DbConnection {
  private final Connection connection;
  private final PreparedStatement testStatement;

                                                       
  public DbConnection(Connection connection, PreparedStatement testStatement) {

    this.connection = connection;
    this.testStatement = testStatement;
  }

  public Connection getConnection() {
    return connection;
  }
  
  public boolean getAutoCommit() {
    try {
      return connection.getAutoCommit();
    } catch (SQLException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }
  
  public void setAutoCommit(boolean autoCommit) {
    try {
      connection.setAutoCommit(autoCommit);
    } catch (SQLException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

  public boolean testConnection() {
    boolean test = true;

    try {
      if (connection.isClosed()) {
        test = false;
      } else if (testStatement != null) {
          testStatement.executeQuery();
      } 
    } catch (SQLException e) {
      test = false;
    }

    return test;
  }
}
