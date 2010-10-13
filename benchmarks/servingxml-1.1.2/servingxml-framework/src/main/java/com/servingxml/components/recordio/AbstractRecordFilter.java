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
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.Key;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.SystemConstants;
import com.servingxml.util.record.ParameterBuilder;
import com.servingxml.util.record.Record;

public abstract class AbstractRecordFilter implements RecordFilter {
  private RecordReader recordReader = RecordReader.NULL;
  private RecordWriter recordWriter = RecordWriter.NULL;

  public AbstractRecordFilter() {            
  }

  public Key getKey() {
    return recordReader.getKey();         
  }

  public Expirable getExpirable() {
    return recordReader.getExpirable();
  }

  public void readRecords(ServiceContext context, Flow flow) {
    recordReader.readRecords(context, flow);
  }

  public void startRecordStream(ServiceContext context, Flow flow) {
    //System.out.println(getClass().getName()+".startRecordStream before "+ recordWriter.getClass().getName());
    recordWriter.startRecordStream(context, flow);
    //System.out.println(getClass().getName()+".startRecordStream after "+ recordWriter.getClass().getName());
  }

  public void endRecordStream(ServiceContext context, Flow flow) {
    recordWriter.endRecordStream(context, flow);
  }                                           

  public void writeRecord(ServiceContext context, Flow flow) {
    //System.out.println(getClass().getName()+".writeRecord before "+ recordWriter.getClass().getName());
    recordWriter.writeRecord(context, flow);
    //System.out.println(getClass().getName()+".writeRecord after "+ recordWriter.getClass().getName());
  }

  public void setRecordReader(RecordReader recordReader) {
    this.recordReader = recordReader;
    this.recordReader.setRecordWriter(this);
  }                       

  public RecordWriter getRecordWriter() {
    return recordWriter;
  }

  public void setRecordWriter(RecordWriter recordWriter) {
    //System.out.println(getClass().getName()+".setRecordWriter "+ recordWriter.getClass().getName());
    this.recordWriter = recordWriter;
  }

  public RecordWriter getDiscardWriter() {
    return recordReader.getDiscardWriter();
  }

  public void setDiscardWriter(RecordWriter discardWriter) {
    //System.out.println(getClass().getName()+".setDiscardWriter " + discardWriter.getClass().getName());
    this.recordReader.setDiscardWriter(discardWriter);
  }

  public RecordReader getRecordReader() {
    return recordReader;
  }

  public void close() { 
    recordWriter.close();
  }

  public void discardRecord(ServiceContext context, Flow flow, ServingXmlException e) {
    recordReader.discardRecord(context, flow, e);
  }
}                                     
