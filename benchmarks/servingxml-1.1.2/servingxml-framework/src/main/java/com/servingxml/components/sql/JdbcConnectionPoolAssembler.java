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

import java.sql.SQLException;

import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.record.Record;

/**
 * The <code>JdbcConnectionPoolAssembler</code> implements an assembler for
 * assembling system <code>JdbcConnectionPool</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class JdbcConnectionPoolAssembler {

  private String driver = "";
  private String databaseUrl = "";
  private String user = "";
  private String password = "";
  private int minConnections = 0;
  private int maxConnections = Integer.MAX_VALUE;
  private String testStatement = "";
  private SqlCommand[] commands = new SqlCommand[0];
                                                       
  public void setDriver(String driver) {
    this.driver = driver;
  }
                                                       
  public void setDatabaseUrl(String databaseUrl) {
    this.databaseUrl = databaseUrl;
  }
                                                       
  public void setUser(String user) {
    this.user = user;
  }
                                                       
  public void setPassword(String password) {
    this.password = password;
  }
                                                       
  public void setMinConnections(int minConnections) {
    this.minConnections = minConnections;
  }
                                                       
  public void setMaxConnections(int maxConnections) {
    this.maxConnections = maxConnections;
  }
  
  public void setTestStatement(String testStatement) {
    this.testStatement = testStatement;
  }

  public void injectComponent(SqlCommand[] commands) {
    this.commands = commands;
  }

  public JdbcConnectionPool assemble(ConfigurationContext context) {

    try {
      SubstitutionExpr driverExpr = SubstitutionExpr.parseString(context.getQnameContext(),driver);
      SubstitutionExpr databaseUrlExpr = SubstitutionExpr.parseString(context.getQnameContext(),databaseUrl);

      String userValue;
      String passwordValue;
      if (user.length() > 0) {
        SubstitutionExpr userExpr = SubstitutionExpr.parseString(context.getQnameContext(),user);
        SubstitutionExpr passwordExpr = SubstitutionExpr.parseString(context.getQnameContext(),password);
        userValue = userExpr.evaluateAsString(context.getParameters(),Record.EMPTY);
        passwordValue = passwordExpr.evaluateAsString(context.getParameters(),Record.EMPTY);
      } else {
        userValue = "";
        passwordValue = "";
      }

      String driverValue = driverExpr.evaluateAsString(context.getParameters(),Record.EMPTY);
      String databaseUrlValue = databaseUrlExpr.evaluateAsString(context.getParameters(),Record.EMPTY);

      JdbcConnectionPool connectionPool = new DefaultJdbcConnectionPool(driverValue, databaseUrlValue,
        userValue, passwordValue, minConnections, maxConnections, testStatement, commands);
      return connectionPool;
    } catch (SQLException e) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_JDBC_CONNECTION_POOL,
        context.getElement().getTagName());
      throw new ServingXmlException(message + "  " + e.getMessage(), e);
    }
  }
}
