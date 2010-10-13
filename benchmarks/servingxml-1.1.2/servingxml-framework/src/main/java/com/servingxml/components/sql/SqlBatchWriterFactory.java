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

import com.servingxml.app.ServiceContext;
import com.servingxml.components.sql.JdbcConnectionPool;
import com.servingxml.components.recordio.AbstractRecordFilterAppender;
import com.servingxml.components.recordio.RecordFilterAppender;
import com.servingxml.components.recordio.RecordWriter;
import com.servingxml.components.recordio.RecordFilter;
import com.servingxml.app.Flow;
import com.servingxml.components.recordio.RecordFilterChain;

/**
 * A <code>SqlBatchWriterFactory</code> instance may be used to obtain objects that
 * implement the <code>RecordWriter</code> interface.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SqlBatchWriterFactory extends AbstractRecordFilterAppender
implements RecordFilterAppender {     

  private final JdbcConnectionPool connectionPool;             
  private final SqlUpdateDatabaseFactory sqlUpdate;
  private final int batchSize;

  public SqlBatchWriterFactory(JdbcConnectionPool connectionPool, SqlUpdateDatabaseFactory sqlUpdate, int batchSize) {
    this.connectionPool = connectionPool;
    this.sqlUpdate = sqlUpdate;
    this.batchSize = batchSize;
  }

  public void appendToRecordPipeline(ServiceContext context, Flow flow,
                                     RecordFilterChain pipeline) {

    SqlBatchUpdateOperation sqlUpdater = sqlUpdate.createSqlBatchUpdater(context,flow);
    RecordFilter recordFilter = new SqlBatchWriter(connectionPool, sqlUpdater, batchSize); 
    pipeline.addRecordFilter(recordFilter);
  }
}
