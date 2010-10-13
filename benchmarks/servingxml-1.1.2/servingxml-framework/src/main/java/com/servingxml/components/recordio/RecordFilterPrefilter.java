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
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.Key;
import com.servingxml.app.Flow;
import com.servingxml.util.ServingXmlException;
import com.servingxml.app.ParameterDescriptor;

public final class RecordFilterPrefilter implements RecordFilter {
  private RecordReader recordReader = RecordReader.NULL;
  private RecordWriter recordWriter = RecordWriter.NULL;
  private final ParameterDescriptor[] parameterDescriptors;

  public RecordFilterPrefilter(ParameterDescriptor[] parameterDescriptors) {
    this.parameterDescriptors = parameterDescriptors;
  }

  public RecordWriter getRecordWriter() {
    return recordWriter;
  }

  public final void setRecordWriter(RecordWriter recordWriter) {
    //System.out.println(getClass().getName()+".setRecordWriter "+ recordWriter.getClass().getName());
    this.recordWriter = recordWriter;
    this.recordReader.setRecordWriter(this);
  }

  public RecordWriter getDiscardWriter() {
    return recordReader.getDiscardWriter();
  }

  public final void setDiscardWriter(RecordWriter discardWriter) {
    this.recordReader.setDiscardWriter(discardWriter);
  }

  public final void readRecords(ServiceContext context, Flow flow) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    recordReader.readRecords(context, newFlow);
  }

  public final Key getKey() {
    return recordReader.getKey();
  }

  public final Expirable getExpirable() {
    return recordReader.getExpirable();
  }

  public final void startRecordStream(ServiceContext context, Flow flow) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    recordWriter.startRecordStream(context, newFlow);
  }
  public final void endRecordStream(ServiceContext context, Flow flow) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    recordWriter.endRecordStream(context, newFlow);
  }
  public final void writeRecord(ServiceContext context, Flow flow) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    recordWriter.writeRecord(context, newFlow);
  }
  public final void close() {
    recordWriter.close();
  }

  public final void setRecordReader(RecordReader recordReader) {
    this.recordReader = recordReader;
  }

  public void discardRecord(ServiceContext context, Flow flow, ServingXmlException e) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    recordReader.discardRecord(context, newFlow, e);
  }
}                         
