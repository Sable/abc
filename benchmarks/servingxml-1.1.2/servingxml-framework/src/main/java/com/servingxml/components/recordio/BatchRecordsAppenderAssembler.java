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

import com.servingxml.app.ServiceContext;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.app.Flow;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.Name;
import com.servingxml.util.NameTest;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.SystemConstants;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.Value;
import com.servingxml.util.record.ParameterBuilder;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public class BatchRecordsAppenderAssembler {
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private String recordType = "*";
  private long batchSize = -1;

  public void setRecordType(String recordType) {
    this.recordType = recordType;
  }

  public void setBatchSize(long batchSize) {
    this.batchSize = batchSize;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }

  public RecordFilterAppender assemble(ConfigurationContext context) {
    //System.out.println(getClass().getName()+".assemble");

    NameTest recordTypeToken = NameTest.parse(context.getQnameContext(), recordType);
    if (batchSize == -1) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
        context.getElement().getTagName(),"batchSize");
      throw new ServingXmlException(message);
    }

    RecordFilterAppender recordFilterAppender = new BatchRecordsAppender(recordTypeToken, batchSize);
    if (parameterDescriptors.length > 0) {
      recordFilterAppender = new RecordFilterAppenderPrefilter(recordFilterAppender,parameterDescriptors);
    }
    return recordFilterAppender;
  }
}

class BatchRecordsAppender extends AbstractRecordFilterAppender       
implements RecordFilterAppender {
  private final NameTest recordTypeToken;
  private final long batchSize;
  
  public BatchRecordsAppender(NameTest recordTypeToken, long batchSize) {
    this.recordTypeToken = recordTypeToken;
    this.batchSize = batchSize;
  }

  public void appendToRecordPipeline(ServiceContext context, Flow flow,
  RecordFilterChain pipeline) {

    RecordFilter recordFilter = new BatchRecords(recordTypeToken, batchSize); 
    pipeline.addRecordFilter(recordFilter);
  }
}

class BatchRecords extends AbstractRecordFilter {
  private static final Name BATCH_SEQUENCE_NUMBER_NAME= new QualifiedName(SystemConstants.SERVINGXML_NS_URI,"batchSequenceNumber");
  private final NameTest recordTypeToken;
  private final long batchSize;
  private long batchSequenceNumber = 0;
  private long recordCount = 0;

  public BatchRecords(NameTest recordTypeToken, long batchSize) {
    this.recordTypeToken = recordTypeToken;
    this.batchSize = batchSize;
  }

  public void writeRecord(ServiceContext context, Flow flow) {
    //System.out.println(getClass().getName()+".writeRecord");

    if (recordTypeToken.matches(flow.getRecord().getRecordType().getName())) {
      if (recordCount >= batchSize) {
        endRecordStream(context, flow);
        startRecordStream(context, flow);
        recordCount = 0;
      }
    }
    ++recordCount;

    super.writeRecord(context, flow);
  }

  public void startRecordStream(ServiceContext context, Flow flow) {
    //System.out.println(getClass().getName()+".startRecordStream");
    this.recordCount = 0;
    ++batchSequenceNumber;
    ParameterBuilder paramBuilder = new ParameterBuilder(flow.getParameters());
    paramBuilder.setLong(BATCH_SEQUENCE_NUMBER_NAME, batchSequenceNumber);
    Record newParameters = paramBuilder.toRecord();
    Flow newFlow = flow.replaceParameters(context, newParameters);
    super.startRecordStream(context, newFlow);
  }

  public void endRecordStream(ServiceContext context, Flow flow) {
    //System.out.println(getClass().getName()+".endRecordStream");

    super.endRecordStream(context, flow);
  }
}
