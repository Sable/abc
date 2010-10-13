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
import java.sql.SQLException;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.util.record.Value;
import com.servingxml.expr.substitution.ValueExpression;
import com.servingxml.util.ServingXmlException;
import com.servingxml.components.recordio.RecordAccepter;
import com.servingxml.util.record.Value;

public class SqlBatchUpdate implements SqlBatchUpdateOperation {
  private final RecordAccepter accepter;
  private final SqlStatement sqlStatement;
  private Statement statement = null;
  private final SqlBatchUpdateOperation[] updateOperations;

  public SqlBatchUpdate(RecordAccepter accepter, SqlStatement sqlStatement, SqlBatchUpdateOperation[] updateOperations) {
    this.accepter = accepter;
    this.sqlStatement = sqlStatement;
    this.updateOperations = updateOperations;
  }

  public void startUpdate(ServiceContext context, Flow flow, Connection connection) {
    //System.out.println(getClass().getName()+".startUpdate");
    try {
      statement = connection.createStatement();
      for (int i = 0; i < updateOperations.length; ++i) {
        updateOperations[i].startUpdate(context,flow,connection);
      }
    } catch (SQLException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

  public void update(ServiceContext context, Flow[] flowHistory, Connection connection) {
    //System.out.println(getClass().getName()+".update");

    int updateCount = 0;
    try {
      for (int i = 0; i < flowHistory.length; ++i) {
        Flow flow = flowHistory[i];
        if (accepter.accept(context, flow, Value.EMPTY)) {
          String sql = sqlStatement.buildSql(context, flow);
          statement.addBatch(sql);
          ++updateCount;
        }
      }
      //System.out.println(getClass().getName()+".update before throw");
      if (updateCount > 0) {
        statement.executeBatch();
        if (updateOperations.length > 0) {
          if (updateCount != flowHistory.length) {
            Flow[] newFlowHistory = new Flow[updateCount];
            int index = 0;
            for (int i = 0; i < flowHistory.length; ++i) {
              Flow flow = flowHistory[i];
              if (accepter.accept(context, flow, Value.EMPTY)) {
                newFlowHistory[index] = flow;
                ++index;
              }
            }
            flowHistory = newFlowHistory;
          }
          for (int i = 0; i < updateOperations.length; ++i) {
            updateOperations[i].update(context,flowHistory,connection);
          }
        }
      }
    } catch (SQLException e) {
      throw new ServingXmlException(e.getMessage(),e);
    } finally {
      try {
        statement.clearBatch();
      } catch (Exception ee) {
      }
    }
  }

  public void endUpdate(ServiceContext context, Flow flow, Connection connection) {
    //System.out.println(getClass().getName()+".endUpdate");
    try {
      for (int i = 0; i < updateOperations.length; ++i) {
        updateOperations[i].endUpdate(context,flow,connection);
      }
      statement.close();
    } catch (SQLException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }
}
