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

import com.servingxml.components.recordio.RecordAccepter;
import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;

public class SqlUpdateFactory implements SqlUpdateDatabaseFactory {
  private final RecordAccepter accepter;  
  private final SqlStatement sqlStatement;
  private final SqlUpdateDatabaseFactory[] detailUpdates;

  public SqlUpdateFactory(RecordAccepter accepter, SqlStatement sqlStatement, SqlUpdateDatabaseFactory[] detailUpdates) {
    this.accepter = accepter;
    this.sqlStatement = sqlStatement;
    this.detailUpdates = detailUpdates;
  }
  
  public SqlUpdateDatabase createSqlUpdater(ServiceContext context, Flow flow) {
    SqlUpdateDatabase[] updateOperations = new SqlUpdateDatabase[detailUpdates.length];
    for (int i = 0; i < detailUpdates.length; ++i) {
      updateOperations[i] = detailUpdates[i].createSqlUpdater(context,flow);
    }
    return new SqlUpdate(accepter, sqlStatement, updateOperations);
  }

  public SqlBatchUpdateOperation createSqlBatchUpdater(ServiceContext context, Flow flow) {
    SqlBatchUpdateOperation[] updateOperations = new SqlBatchUpdateOperation[detailUpdates.length];
    for (int i = 0; i < detailUpdates.length; ++i) {
      updateOperations[i] = detailUpdates[i].createSqlBatchUpdater(context,flow);
    }
    return new SqlBatchUpdate(accepter, sqlStatement, updateOperations);
  }
}
