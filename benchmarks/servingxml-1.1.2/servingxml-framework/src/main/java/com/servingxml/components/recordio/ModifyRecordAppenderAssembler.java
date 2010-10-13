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
import com.servingxml.util.record.RecordBuilder;
import com.servingxml.util.record.Value;
import com.servingxml.app.ParameterDescriptor;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public class ModifyRecordAppenderAssembler {
  private Name newRecordTypeName = Name.EMPTY;
  private NewField[] newFields = NewField.EMPTY_ARRAY;
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;

  public void setNewRecordType(Name newRecordTypeName) {
    this.newRecordTypeName = newRecordTypeName;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(NewField[] newFields) {
    this.newFields = newFields;
  }

  public RecordFilterAppender assemble(ConfigurationContext context) {

    RecordFilterAppender recordFilterAppender = new ModifyRecordAppender(newRecordTypeName, newFields);
    if (parameterDescriptors.length > 0) {
      recordFilterAppender = new RecordFilterAppenderPrefilter(recordFilterAppender,parameterDescriptors);
    }
    return recordFilterAppender;
  }
}

class ModifyRecordAppender extends AbstractRecordFilterAppender       
implements RecordFilterAppender {
  private final Name newRecordTypeName;
  private final NewField[] newFields;
  
  public ModifyRecordAppender(Name newRecordTypeName, NewField[] newFields) {
    this.newFields = newFields;
    this.newRecordTypeName = newRecordTypeName;
  }

  public void appendToRecordPipeline(ServiceContext context, Flow flow,
  RecordFilterChain pipeline) {

    RecordFilter recordFilter = new ModifyRecord(newRecordTypeName, newFields); 
    pipeline.addRecordFilter(recordFilter);
  }
}

class ModifyRecord extends AbstractRecordFilter {
  private final Name newRecordTypeName;
  private final NewField[] newFields;

  public ModifyRecord(Name newRecordTypeName, NewField[] newFields) {
    this.newRecordTypeName = newRecordTypeName;
    this.newFields = newFields;
  }

  public void writeRecord(ServiceContext context, Flow flow) {

    RecordBuilder recordBuilder;
    if (newRecordTypeName.isEmpty()) {
      recordBuilder = new RecordBuilder(flow.getRecord());
    } else {
      recordBuilder = new RecordBuilder(newRecordTypeName, flow.getRecord());
    }

    for (int i = 0; i < newFields.length; ++i) {
      NewField newField = newFields[i];
      newField.readField(context, flow, recordBuilder);
    }
    Flow newFlow = flow.replaceRecord(context, recordBuilder.toRecord());

    getRecordWriter().writeRecord(context, newFlow);
  }
}
