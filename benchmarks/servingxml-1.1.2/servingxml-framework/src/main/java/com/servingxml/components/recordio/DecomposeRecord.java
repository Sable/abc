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

package com.servingxml.components.recordio;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.Name;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordBuilder;
import com.servingxml.util.record.Value;

/**
 *
 * 
 * @author  Daniel A. Parker
 */


class DecomposeRecord extends AbstractRecordFilter {
  private final Name compositeRecordTypeName;
  private final Name repeatingGroupName;
  private final Name recordTypeName;

  public DecomposeRecord(Name compositeRecordTypeName, Name repeatingGroupName, Name recordTypeName) {
    this.compositeRecordTypeName = compositeRecordTypeName;
    this.repeatingGroupName = repeatingGroupName;
    this.recordTypeName = recordTypeName;
  }

  public void writeRecord(ServiceContext context, Flow flow) {
    Record record = flow.getRecord();
    if (record.getRecordType().getName().equals(compositeRecordTypeName)) {
      Value value = record.getValue(repeatingGroupName);
      if (value != null) {
        Record[] subrecords = value.getRecords();
    
        for (int i = 0; i < subrecords.length; ++i) {
          Record subrecord = subrecords[i];
          Name typeName = recordTypeName.isEmpty() ? subrecord.getRecordType().getName() : recordTypeName;
          RecordBuilder builder = new RecordBuilder(typeName);
          for (int j = 0; j < record.fieldCount(); ++j) {
            Name namej = record.getFieldName(j);
            Value valuej = record.getValue(j);
            if (!namej.equals(repeatingGroupName)) {
              builder.setValue(namej,valuej);
            } else {
              for (int k = 0; k < subrecord.fieldCount(); ++k) {
                Value valuek = subrecord.getValue(k);
                Name namek = subrecord.getFieldName(k);
                builder.setValue(namek,valuek);
              }
            }
          }
          Record newRecord = builder.toRecord();
          Flow newFlow = flow.replaceRecord(context,newRecord);
          super.writeRecord(context,newFlow);
        }
      }
    } else {
      super.writeRecord(context,flow);
    }
  }
}
