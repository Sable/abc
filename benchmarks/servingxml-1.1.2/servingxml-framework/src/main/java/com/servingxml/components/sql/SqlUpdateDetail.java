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
import com.servingxml.util.record.Record;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.Name;
import com.servingxml.util.record.Value;

public class SqlUpdateDetail implements SqlUpdateDatabase {
  private final Name fieldName;
  private final SqlUpdateDatabase sqlUpdater;
  
  public SqlUpdateDetail(Name fieldName, SqlUpdateDatabase sqlUpdater) {
    this.fieldName = fieldName;
    this.sqlUpdater = sqlUpdater;
  }

  public void startUpdate(ServiceContext context, Flow flow, Connection connection) {
    //System.out.println(getClass().getName()+".startUpdate");
    sqlUpdater.startUpdate(context,flow,connection);
  }

  public void update(ServiceContext context, Flow flow, Connection connection) {
    //System.out.println(getClass().getName()+".update");
    Record record = flow.getRecord();
    //System.out.println("record="+record.toXmlString(context));
    Value value = record.getValue(fieldName);
    if (value != null) {
      Record[] subrecords = value.getRecords();
      for (int i = 0; i < subrecords.length; ++i) {
        Record subrecord = subrecords[i];
        //System.out.println("subrecord="+subrecord.toXmlString(context));
        Flow newFlow = flow.replaceRecord(context, subrecord);
        sqlUpdater.update(context,newFlow,connection);
      }
    }
  }

  public void endUpdate(ServiceContext context, Flow flow, Connection connection) {
    sqlUpdater.endUpdate(context,flow,connection);
  }
}
