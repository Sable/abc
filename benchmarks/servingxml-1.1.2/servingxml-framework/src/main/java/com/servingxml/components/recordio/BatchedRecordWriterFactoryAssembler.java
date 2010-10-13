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

public class BatchedRecordWriterFactoryAssembler {
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private String recordType = "*";
  private long batchSize = -1;
  private RecordWriterFactory recordWriterFactory;

  public void setRecordType(String recordType) {
    this.recordType = recordType;
  }

  public void setBatchSize(long batchSize) {
    this.batchSize = batchSize;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(RecordWriterFactory recordWriterFactory) {

    this.recordWriterFactory = recordWriterFactory;
  }

  public RecordWriterFactory assemble(ConfigurationContext context) {
    //System.out.println(getClass().getName()+".assemble");

    NameTest recordTypeToken = NameTest.parse(context.getQnameContext(), recordType);
    if (batchSize == -1) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
        context.getElement().getTagName(),"batchSize");
      throw new ServingXmlException(message);
    }

    RecordWriterFactory rwf = new BatchedRecordWriterFactory(recordWriterFactory, recordTypeToken, batchSize);
    if (parameterDescriptors.length > 0) {
      rwf = new RecordWriterFactoryPrefilter(rwf,parameterDescriptors);
    }
    return rwf;
  }
}

class BatchedRecordWriterFactory extends AbstractRecordWriterFactory       
implements RecordWriterFactory {
  private final RecordWriterFactory recordWriterFactory;
  private final NameTest recordTypeToken;
  private final long batchSize;
  
  public BatchedRecordWriterFactory(RecordWriterFactory recordWriterFactory, NameTest recordTypeToken, long batchSize) {
    this.recordWriterFactory = recordWriterFactory;
    this.recordTypeToken = recordTypeToken;
    this.batchSize = batchSize;
  }

  public RecordWriter createRecordWriter(ServiceContext context, Flow flow) {
    RecordWriter recordWriter = recordWriterFactory.createRecordWriter(context,flow);
    RecordWriter rw = new BatchedRecordWriter(recordWriter, recordTypeToken, batchSize); 
    return rw;
  }
}

class BatchedRecordWriter extends AbstractRecordWriter implements RecordWriter {
  private static final Name BATCH_SEQUENCE_NUMBER_NAME= new QualifiedName(SystemConstants.SERVINGXML_NS_URI,"batchSequenceNumber");
  private final RecordWriter recordWriter;
  private final NameTest recordTypeToken;
  private final long batchSize;
  private long batchSequenceNumber = 0;
  private long recordCount = 0;

  public BatchedRecordWriter(RecordWriter recordWriter, NameTest recordTypeToken, long batchSize) {
    this.recordWriter = recordWriter;
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

    recordWriter.writeRecord(context, flow);
  }

  public void startRecordStream(ServiceContext context, Flow flow) {
    //System.out.println(getClass().getName()+".startRecordStream");
    this.recordCount = 0;
    ++batchSequenceNumber;
    ParameterBuilder paramBuilder = new ParameterBuilder(flow.getParameters());
    paramBuilder.setLong(BATCH_SEQUENCE_NUMBER_NAME, batchSequenceNumber);
    Record newParameters = paramBuilder.toRecord();
    Flow newFlow = flow.replaceParameters(context, newParameters);
    recordWriter.startRecordStream(context, newFlow);
  }

  public void endRecordStream(ServiceContext context, Flow flow) {
    //System.out.println(getClass().getName()+".endRecordStream");

    recordWriter.endRecordStream(context, flow);
  }

  public void close() {
    recordWriter.close();
  }
}
