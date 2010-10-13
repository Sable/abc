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

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.util.Name;

public class SqlUpdateDetailFactory implements SqlUpdateDatabaseFactory {
  private final Name fieldName;
  private final SqlUpdateDatabaseFactory sqlUpdate;
  
  public SqlUpdateDetailFactory(Name fieldName, SqlUpdateDatabaseFactory sqlUpdate) {
    this.fieldName = fieldName;
    this.sqlUpdate = sqlUpdate;
  }
  
  public SqlUpdateDatabase createSqlUpdater(ServiceContext context, Flow flow) {
    SqlUpdateDatabase updater = sqlUpdate.createSqlUpdater(context,flow);
    return new SqlUpdateDetail(fieldName, updater);
  }

  public SqlBatchUpdateOperation createSqlBatchUpdater(ServiceContext context, Flow flow) {
    SqlBatchUpdateOperation updater = sqlUpdate.createSqlBatchUpdater(context,flow);
    return new SqlBatchUpdateDetail(fieldName, updater);
  }
}
