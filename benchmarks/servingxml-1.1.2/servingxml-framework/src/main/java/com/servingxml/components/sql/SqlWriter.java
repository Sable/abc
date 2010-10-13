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
import com.servingxml.components.recordio.AbstractRecordWriter;
import com.servingxml.util.ServingXmlException;

/**                              
 *
 * 
 * @author  Daniel A. Parker
 */

public class SqlWriter extends AbstractRecordWriter implements RecordWriter {
  
  private final JdbcConnectionPool connectionPool;
  private final SqlUpdateDatabase sqlUpdater;
  private Connection connection = null;
  private boolean lastAutoCommit = false;

  public SqlWriter(JdbcConnectionPool connectionPool, SqlUpdateDatabase sqlUpdater) {
    
    this.connectionPool = connectionPool;
    this.sqlUpdater = sqlUpdater;
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
    try {
      //System.out.println(getClass().getName()+".endRecordStream");
      sqlUpdater.endUpdate(context, flow, connection);
      close();
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(),e);
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
    try {
      //System.out.println(getClass().getName()+".writeRecord");
      sqlUpdater.update(context, flow, connection);
      connection.commit();
    } catch (Exception e) {
      try {
        connection.rollback();
      } catch (Exception t) {
      }
      ServingXmlException reason = new ServingXmlException(e.getMessage(), e);
      throw reason;
    }
  }
}

