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
import com.servingxml.components.recordio.AbstractRecordWriterFactory;
import com.servingxml.components.recordio.RecordWriter;
import com.servingxml.components.recordio.RecordWriterFactory;
import com.servingxml.app.Flow;

/**
 * A <code>SqlWriterFactory</code> instance may be used to obtain objects that
 * implement the <code>RecordWriter</code> interface.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SqlWriterFactory extends AbstractRecordWriterFactory
implements RecordWriterFactory {     

  private final JdbcConnectionPool connectionPool;             
  private final SqlUpdateDatabaseFactory sqlUpdate;

  public SqlWriterFactory(JdbcConnectionPool connectionPool, SqlUpdateDatabaseFactory sqlUpdate) {
    this.connectionPool = connectionPool;
    this.sqlUpdate = sqlUpdate;
  }

  public RecordWriter createRecordWriter(ServiceContext context, Flow flow) {
    SqlUpdateDatabase sqlUpdater = sqlUpdate.createSqlUpdater(context, flow);
    RecordWriter recordWriter = new SqlWriter(connectionPool, sqlUpdater);
    return recordWriter;
  }
}
