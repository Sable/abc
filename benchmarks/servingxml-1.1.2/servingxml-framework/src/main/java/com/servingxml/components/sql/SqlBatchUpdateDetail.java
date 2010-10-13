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

public class SqlBatchUpdateDetail implements SqlBatchUpdateOperation {
  private final Name fieldName;
  private final SqlBatchUpdateOperation sqlUpdater;
  
  public SqlBatchUpdateDetail(Name fieldName, SqlBatchUpdateOperation sqlUpdater) {
    this.fieldName = fieldName;
    this.sqlUpdater = sqlUpdater;
  }

  public void startUpdate(ServiceContext context, Flow flow, Connection connection) {
    //System.out.println(getClass().getName()+".startUpdate");
    sqlUpdater.startUpdate(context,flow,connection);
  }

  public void update(ServiceContext context, Flow[] flowHistory, Connection connection) {
    int detailCount = 0;
    for (int i = 0; i < flowHistory.length; ++i) {
      Flow flow = flowHistory[i];
      Record record = flow.getRecord();
      Value value = record.getValue(fieldName);
      if (value != null) {
        Record[] subrecords = value.getRecords();
        detailCount += subrecords.length;
      }
    }
    if (detailCount > 0) {
      Flow[] detailHistory = new Flow[detailCount];
      int index = 0;
      for (int i = 0; i < flowHistory.length; ++i) {
        Flow flow = flowHistory[i];
        Record record = flow.getRecord();
        Value value = record.getValue(fieldName);
        if (value != null) {
          Record[] subrecords = value.getRecords();
          for (int j = 0; j < subrecords.length; ++j) {
            Record subrecord = subrecords[j];
            detailHistory[index++] = flow.replaceRecord(context, subrecord);
          }
        }
      }
      sqlUpdater.update(context,detailHistory,connection);
    }
  }

  public void endUpdate(ServiceContext context, Flow flow, Connection connection) {
    sqlUpdater.endUpdate(context,flow,connection);
  }
}
