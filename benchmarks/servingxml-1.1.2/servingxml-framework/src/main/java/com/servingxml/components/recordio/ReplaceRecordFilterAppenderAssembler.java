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
import com.servingxml.util.record.Record;
import com.servingxml.util.record.Value;
import com.servingxml.app.ParameterDescriptor;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public class ReplaceRecordFilterAppenderAssembler {
  private Name recordTypeName = Name.EMPTY;
  private NewRecord[] newRecords = new NewRecord[0];
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;

  public void setRecordType(Name recordTypeName) {
    this.recordTypeName = recordTypeName;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(NewRecord[] newRecords) {
    this.newRecords = newRecords;
  }

  public RecordFilterAppender assemble(ConfigurationContext context) {

    RecordFilterAppender recordFilterAppender = new ReplaceRecordFilterAppender(recordTypeName, newRecords);
    if (parameterDescriptors.length > 0) {
      recordFilterAppender = new RecordFilterAppenderPrefilter(recordFilterAppender,parameterDescriptors);
    }
    return recordFilterAppender;
  }
}

class ReplaceRecordFilterAppender extends AbstractRecordFilterAppender       
implements RecordFilterAppender {
  private final Name recordTypeName;
  private final NewRecord[] newRecords;
  
  public ReplaceRecordFilterAppender(Name recordTypeName, NewRecord[] newRecords) {
    this.recordTypeName = recordTypeName;
    this.newRecords = newRecords;
  }

  public void appendToRecordPipeline(ServiceContext context, Flow flow,
  RecordFilterChain pipeline) {

    RecordFilter recordFilter = new ReplaceRecordFilter(recordTypeName, newRecords); 
    pipeline.addRecordFilter(recordFilter);
  }
}

class ReplaceRecordFilter extends AbstractRecordFilter {
  private final Name recordTypeName;
  private final NewRecord[] newRecords;

  public ReplaceRecordFilter(Name recordTypeName, NewRecord[] newRecords) {
    this.recordTypeName = recordTypeName;
    this.newRecords = newRecords;
  }

  public void writeRecord(ServiceContext context, Flow flow) {

    Record record = flow.getRecord();
    if (recordTypeName.isEmpty() || recordTypeName.equals(record.getRecordType().getName())) {
      for (int i = 0; i < newRecords.length; ++i) {
        NewRecord newRecord = newRecords[i];
        Record rec = newRecord.newRecord(context, flow);
        Flow newFlow = flow.replaceRecord(context, rec);
        getRecordWriter().writeRecord(context, newFlow);
      }
    } else {
      getRecordWriter().writeRecord(context, flow);
    }
  }
}
