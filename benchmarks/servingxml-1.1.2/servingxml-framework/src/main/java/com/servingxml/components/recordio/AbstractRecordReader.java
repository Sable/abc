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
import com.servingxml.io.cache.DefaultKey;
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.Key;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.SystemConstants;
import com.servingxml.util.record.ParameterBuilder;
import com.servingxml.util.record.Record;

public abstract class AbstractRecordReader implements RecordReader {
  private RecordWriter recordWriter = RecordWriter.NULL;
  private RecordWriter discardWriter = RecordWriter.DEFAULT_DISCARD_WRITER;
  
  public AbstractRecordReader() {            
  }

  public Key getKey() {
    return DefaultKey.newInstance();         
  }

  public Expirable getExpirable() {
    return Expirable.IMMEDIATE_EXPIRY;
  }

  public RecordWriter getRecordWriter() {
    return recordWriter;
  }

  public void setRecordWriter(RecordWriter recordWriter) {
    this.recordWriter = recordWriter;
  }

  public RecordWriter getDiscardWriter() {
    return discardWriter;
  }

  public void setDiscardWriter(RecordWriter discardWriter) {
    this.discardWriter = discardWriter;
  }

  public void close() {
    recordWriter.close();
    discardWriter.close();
  }

  public void startRecordStream(ServiceContext context, Flow flow) {
    discardWriter.startRecordStream(context, flow);
    recordWriter.startRecordStream(context, flow);
  }

  public void endRecordStream(ServiceContext context, Flow flow) {
    recordWriter.endRecordStream(context, flow);
    discardWriter.endRecordStream(context, flow);
  }

  public void writeRecord(ServiceContext context, Flow flow) {
    recordWriter.writeRecord(context, flow);
  }

  public void discardRecord(ServiceContext context, Flow flow, ServingXmlException e) {
    String message = e.getMessage();
    String s;
    Record record = flow.getRecord();
    if (flow.getCurrentLineNumber() != 0) {
      s = "Error in record \"" + record.getRecordType().getName() + "\" on line " + flow.getCurrentLineNumber() + ".  " + message;
    } else {
      s = "Error in record \"" + record.getRecordType().getName() + "\".  " + message;
    }
    ParameterBuilder paramBuilder = new ParameterBuilder(flow.getParameters());
    paramBuilder.setString(SystemConstants.MESSAGE_NAME,s);
    Record newParameters = paramBuilder.toRecord();
    Flow newFlow = flow.replaceParameters(context, newParameters);
    discardWriter.writeRecord(context, newFlow);
  }
}
