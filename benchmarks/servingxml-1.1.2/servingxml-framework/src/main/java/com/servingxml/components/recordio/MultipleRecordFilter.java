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
import com.servingxml.app.Flow;
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.ExpirableFamily;
import com.servingxml.util.ServingXmlException;
import com.servingxml.io.cache.Key;

public class MultipleRecordFilter implements RecordFilter, RecordFilterChain {
  private final ExpirableFamily expirableFamily = new ExpirableFamily();
  private RecordFilter head;
  private RecordFilter tail = RecordFilter.NULL;

  public void addRecordFilter(RecordFilter recordFilter) {
    recordFilter.setRecordReader(tail);
    if (this.head == null) {
      this.head = recordFilter;
    }
    this.tail = recordFilter;
  }

  public void addExpirable(Expirable expirable) {
    expirableFamily.addExpirable(expirable);
  }

  public void setRecordReader(RecordReader recordReader) {
    tail.setRecordReader(recordReader);
  }
                       
  public void readRecords(ServiceContext context, Flow flow) {
      tail.readRecords(context, flow);
  }

  public final Key getKey() {
    return tail.getKey();
  }

  public final Expirable getExpirable() {
    return expirableFamily;
  }

  public final RecordWriter getRecordWriter() {
    return tail.getRecordWriter();
  }

  public final void setRecordWriter(RecordWriter recordWriter) {
    tail.setRecordWriter(recordWriter);
  }

  public final RecordWriter getDiscardWriter() {
    return tail.getDiscardWriter();
  }

  public final void setDiscardWriter(RecordWriter discardWriter) {
    tail.setDiscardWriter(discardWriter);
  }

  public void startRecordStream(ServiceContext context, Flow flow) {
    if (head != null) {
      //System.out.println(getClass().getName()+".startRecordStream " + head.getClass().getName());
      head.startRecordStream(context,flow);
    }
  }

  public void endRecordStream(ServiceContext context, Flow flow) {
    if (head != null) {
      //System.out.println(getClass().getName()+".endRecordStream " + head.getClass().getName());
      head.endRecordStream(context,flow);
    }
  }

  public void writeRecord(ServiceContext context, Flow flow) {
    if (head != null) {
      //System.out.println(getClass().getName()+".writeRecord before " + head.getClass().getName());
      head.writeRecord(context,flow);
      //System.out.println(getClass().getName()+".writeRecord after " + head.getClass().getName());
    }
  }

  public void close() {
    if (head != null) {
      head.close();
    }
  }

  public void discardRecord(ServiceContext context, Flow flow, ServingXmlException e) {
    if (head != null) {
      //System.out.println(getClass().getName()+".writeRecord before " + head.getClass().getName());
      head.discardRecord(context,flow,e);
      //System.out.println(getClass().getName()+".writeRecord after " + head.getClass().getName());
    }
  }
}   

