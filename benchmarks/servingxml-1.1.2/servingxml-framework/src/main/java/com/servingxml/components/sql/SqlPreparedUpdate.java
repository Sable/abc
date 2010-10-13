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
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.Value;
import com.servingxml.util.QualifiedName;
import com.servingxml.components.recordio.RecordAccepter;
import com.servingxml.util.record.Value;

public class SqlPreparedUpdate implements SqlUpdateDatabase {
  private final RecordAccepter accepter;
  private final String statement;
  private final ValueExpression[] arguments;
  private PreparedStatement preparedStatement = null;
  private final SqlUpdateDatabase[] updateOperations;
  
  public SqlPreparedUpdate(RecordAccepter accepter, String statement, ValueExpression[] arguments,
                           SqlUpdateDatabase[] updateOperations) {
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

  public void update(ServiceContext context, Flow flow, Connection connection) {

    if (accepter.accept(context, flow, Value.EMPTY)) {
      Record record = flow.getRecord();
      try {
        for (int i = 0; i < arguments.length; ++i) {
          ValueExpression expression = arguments[i];
          Value value = expression.evaluateValue(flow.getParameters(), record);
          Object a = value.getSqlValue();
          if (a != null) {
            preparedStatement.setObject(i+1, a);
          } else {
            preparedStatement.setNull(i+1, value.getSqlType());
          }
        }
        preparedStatement.executeUpdate();
        for (int i = 0; i < updateOperations.length; ++i) {
          updateOperations[i].update(context,flow,connection);
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
