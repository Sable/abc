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
import java.sql.SQLException;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.sql.JdbcConnectionPool;
import com.servingxml.components.recordio.RecordWriter;
import com.servingxml.components.recordio.AbstractRecordFilter;
import com.servingxml.util.ServingXmlException;

/**                              
 *
 * 
 * @author  Daniel A. Parker
 */

public class SqlBatchWriter extends AbstractRecordFilter implements RecordWriter {

  private final JdbcConnectionPool connectionPool;
  private final SqlBatchUpdateOperation sqlUpdater;
  private Connection connection = null;
  private boolean lastAutoCommit = false;
  private final Flow[] flowHistory;
  private int batchCounter = 0;

  public SqlBatchWriter(JdbcConnectionPool connectionPool, SqlBatchUpdateOperation sqlUpdater, int batchSize) {

    this.connectionPool = connectionPool;
    this.sqlUpdater = sqlUpdater;
    this.flowHistory = new Flow[batchSize];
  }

  public void startRecordStream(ServiceContext context, Flow flow) {
    try {
      //System.out.println(getClass().getName()+".startRecordStream");
      this.connection = connectionPool.getConnection();
      this.lastAutoCommit = connection.getAutoCommit();
      this.connection.setAutoCommit(false);
      sqlUpdater.startUpdate(context, flow, connection);
    } catch (SQLException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

  public void endRecordStream(ServiceContext context, Flow flow) {
    //System.out.println(getClass().getName()+".endRecordStream");
    try {
      if (batchCounter > 0) {
        submitBatch(context);
      }
      sqlUpdater.endUpdate(context, flow,connection);
    } finally {
      batchCounter = 0;
      try {
        close();
      } catch (Exception t) {
        //  Don't care                                       
      }
    }
  }

  public void close() {
    if (connection != null) {
      try {
        connection.setAutoCommit(lastAutoCommit);
        connectionPool.releaseConnection(connection);
      } catch (SQLException e) {
        throw new ServingXmlException(e.getMessage(), e);
      } finally {
        connection = null;
      }
    }
  }

  public void writeRecord(ServiceContext context, Flow flow) {
    //System.out.println(getClass().getName()+".writeRecord");
    try {
      flowHistory[batchCounter++] = flow;
      if (batchCounter >= flowHistory.length) {
        submitBatch(context);
      }
    } catch (ServingXmlException e) {
      throw e;
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(), e);
    } finally {
      batchCounter = 0;
    }
  }

  private void submitBatch(ServiceContext context) {
    try {
      sqlUpdater.update(context, flowHistory, connection);
      connection.commit();
    } catch (Exception e) {
      try {
        connection.rollback();
      } catch (Exception t) {
      }
      String message = "Error in batch " + batchCounter + ". " + e.getMessage();
      ServingXmlException reason = new ServingXmlException(message, e);
      for (int i = 0; i < flowHistory.length; ++i) {
        Flow flow = flowHistory[i];
        discardRecord(context, flow, reason);
      }
    } finally {
      batchCounter = 0;
    }
  }
}

