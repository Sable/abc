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
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.app.ServiceContext;
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.Key;
import com.servingxml.util.ServingXmlException;

public class RecordReaderPrefilter implements RecordReader {
  private final RecordReader recordReader;
  private final ParameterDescriptor[] parameterDescriptors;

  public RecordReaderPrefilter(RecordReader recordReader,
  ParameterDescriptor[] parameterDescriptors) {
    this.recordReader = recordReader;
    this.parameterDescriptors = parameterDescriptors;
  }

  public RecordWriter getRecordWriter() {
    return recordReader.getRecordWriter();
  }

  public void setRecordWriter(RecordWriter recordWriter) {
    recordReader.setRecordWriter(new RecordWriterPrefilter(recordWriter,parameterDescriptors));
  }

  public RecordWriter getDiscardWriter() {
    return recordReader.getDiscardWriter();
  }

  public void setDiscardWriter(RecordWriter discardWriter) {
    recordReader.setDiscardWriter(discardWriter);
  }

  public void readRecords(ServiceContext context, Flow flow) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    recordReader.readRecords(context,newFlow);
  }

  public Key getKey() {
    return recordReader.getKey();
  }

  public Expirable getExpirable() {
    return recordReader.getExpirable();
  }

  public void discardRecord(ServiceContext context, Flow flow, ServingXmlException e) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    recordReader.discardRecord(context, newFlow, e);
  }
}
