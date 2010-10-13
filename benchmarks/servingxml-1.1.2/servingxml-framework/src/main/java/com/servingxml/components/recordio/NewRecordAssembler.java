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
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.Name;
import com.servingxml.util.NameTest;
import com.servingxml.util.record.FieldType;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordType;
import com.servingxml.util.record.RecordBuilder;
import com.servingxml.util.record.Value;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public class NewRecordAssembler {
  private Name newRecordTypeName = Name.EMPTY;
  private NewField[] newFields = NewField.EMPTY_ARRAY;
  private String fieldQnames = "*";
  private String exceptQnames = "";

  public void setRecordType(Name newRecordTypeName) {
    this.newRecordTypeName = newRecordTypeName;
  }

  public void injectComponent(NewField[] newFields) {
    this.newFields = newFields;
  }

  public void setFields(String fieldQnames) {
    this.fieldQnames = fieldQnames;
  }

  public void setExcept(String exceptQnames) {
    this.exceptQnames = exceptQnames;
  }

  public void setExceptFields(String exceptQnames) {
    this.exceptQnames = exceptQnames;
  }

  public NewRecord assemble(ConfigurationContext context) {

    NameTest fields = NameTest.parse(context.getQnameContext(), fieldQnames);
    NameTest except = NameTest.parse(context.getQnameContext(), exceptQnames);

    return new NewRecord(newRecordTypeName, fields, except, newFields);
  }
}

class NewRecord {
  private final Name newRecordTypeName;
  private final NameTest fields;
  private final NameTest except;
  private final NewField[] newFields;
  
  public NewRecord(Name newRecordTypeName, NameTest fields, NameTest except, NewField[] newFields) {
    this.newRecordTypeName = newRecordTypeName;
    this.fields = fields;
    this.except = except;
    this.newFields = newFields;
  }

  public Record newRecord(ServiceContext context, Flow flow) {

    Record record = flow.getRecord();
    Name recordTypeName = newRecordTypeName.isEmpty() ? record.getRecordType().getName() : newRecordTypeName;

    final RecordBuilder recordBuilder = new RecordBuilder(recordTypeName);

    RecordType recordType = record.getRecordType();
    for (int i = 0; i < recordType.count(); ++i) {
      FieldType fieldType = recordType.getFieldType(i);
      Value value = record.getValue(i);
      Name fieldName = fieldType.getName();
      if (fields.matches(fieldName) && !except.matches(fieldName)) {
        recordBuilder.setValue(fieldName, value);
      }
    }

    for (int i = 0; i < newFields.length; ++i) {
      NewField field = newFields[i];
      field.readField(context, flow,recordBuilder);
    }
    return recordBuilder.toRecord();
  }
}

