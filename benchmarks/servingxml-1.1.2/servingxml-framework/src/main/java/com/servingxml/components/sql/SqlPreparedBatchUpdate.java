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

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.expr.substitution.ValueExpression;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Value;
import com.servingxml.components.recordio.RecordAccepter;

public class SqlPreparedBatchUpdate implements SqlBatchUpdateOperation {
  private final RecordAccepter accepter;
  private final String statement;
  private final ValueExpression[] arguments;
  private PreparedStatement preparedStatement = null;
  private final SqlBatchUpdateOperation[] updateOperations;

  public SqlPreparedBatchUpdate(RecordAccepter accepter, String statement, ValueExpression[] arguments, 
                                SqlBatchUpdateOperation[] updateOperations) {

    this.accepter = accepter;
    this.statement = statement;
    this.arguments = arguments;
    this.updateOperations = updateOperations;
  }

  public void startUpdate(ServiceContext context, Flow flow, Connection connection) {
    try {
      preparedStatement = connection.prepareStatement(statement);
      for (int i = 0; i < updateOperations.length; ++i) {
        updateOperations[i].startUpdate(context,flow,connection);
      }
    } catch (SQLException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

  public void update(ServiceContext context, Flow[] flowHistory, Connection connection) {

    try {
      int updateCount = 0;
      for (int i = 0; i < flowHistory.length; ++i) {
        Flow flow = flowHistory[i];
        if (accepter.accept(context, flow, Value.EMPTY)) {
          //System.out.println("arguments="+arguments.length);
          for (int j = 0; j < arguments.length; ++j) {
            ValueExpression expression = arguments[j];
            //System.out.println( flow.getParameters().toXmlString(context));
            Value value = expression.evaluateValue(flow.getParameters(),flow.getRecord());
            //System.out.println(value.getString());
            Object a = value.getSqlValue();
            if (a != null) {
              preparedStatement.setObject(j+1, a);
            } else {
              //System.out.println("Is null");
              preparedStatement.setNull(j+1, value.getSqlType());
            }
          }
          preparedStatement.addBatch();
          ++updateCount;
        }
      }
      if (updateCount > 0) {
        preparedStatement.executeBatch();
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
      try {
        preparedStatement.cancel();
      } catch (Exception e2) {
      }
      throw new ServingXmlException(e.getMessage(),e);
    } catch (RuntimeException e) {
      try {
        preparedStatement.cancel();
      } catch (Exception e2) {
      }
      throw new ServingXmlException(e.getMessage(),e);
    } finally {
      try {
        preparedStatement.clearBatch();
      } catch (Exception ee) {
      }
    }
  }

  public void endUpdate(ServiceContext context, Flow flow, Connection connection) {
    try {
      for (int i = 0; i < updateOperations.length; ++i) {
        updateOperations[i].endUpdate(context,flow,connection);
      }
      preparedStatement.close();
    } catch (SQLException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }
}
