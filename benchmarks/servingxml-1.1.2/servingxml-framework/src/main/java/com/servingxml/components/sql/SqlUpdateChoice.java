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
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.util.ServingXmlException;

public class SqlUpdateChoice implements SqlUpdateDatabase {
  private final SqlQuery sqlQuery;
  private final SqlUpdateDatabase sqlFoundUpdater;
  private final SqlUpdateDatabase sqlNotFoundUpdater;
  
  public SqlUpdateChoice(SqlQuery sqlQuery, SqlUpdateDatabase sqlFoundUpdater,
                      SqlUpdateDatabase sqlNotFoundUpdater) {
    this.sqlQuery = sqlQuery;
    this.sqlFoundUpdater = sqlFoundUpdater;
    this.sqlNotFoundUpdater = sqlNotFoundUpdater;
  }

  public void startUpdate(ServiceContext context, Flow flow, Connection connection) {
    sqlFoundUpdater.startUpdate(context, flow, connection);
    sqlNotFoundUpdater.startUpdate(context, flow, connection);
  }

  public void update(ServiceContext context, Flow flow, Connection connection) {
    ResultSet resultSet = null;
    Statement stat = null;
    String sql = sqlQuery.buildSql(context, flow);

    boolean found;
    try {
      try {

        stat = connection.createStatement();
        resultSet = stat.executeQuery(sql);
        found = resultSet.next();
      } finally {
        if (resultSet != null) {
          try {
            resultSet.close();
          } catch (Exception rse) {
            //  Don't care
          }
        }
        if (stat != null) {
          try {                   
            stat.close();
          } catch (Exception se) {
            //  Don't care
          }
        }
      }
      if (found) {
        if (sqlFoundUpdater != null) {
          sqlFoundUpdater.update(context, flow, connection);
        }
      } else {
        if (sqlNotFoundUpdater != null) {
          sqlNotFoundUpdater.update(context, flow, connection);
        }
      }
    } catch (SQLException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

  public void endUpdate(ServiceContext context, Flow flow, Connection connection) {
    sqlFoundUpdater.endUpdate(context, flow, connection);
    sqlNotFoundUpdater.endUpdate(context, flow, connection);
  }
}
