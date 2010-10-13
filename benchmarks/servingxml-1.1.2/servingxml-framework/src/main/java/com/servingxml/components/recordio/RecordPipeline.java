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
import com.servingxml.io.cache.Key;

public class RecordPipeline implements RecordFilterChain {
  private final Flow flow;
  private final ExpirableFamily expirableFamily = new ExpirableFamily();
  private RecordReader recordReader;

  public RecordPipeline(Flow flow, RecordReader recordReader) {

    this.flow = flow;
    this.recordReader = recordReader;

    Expirable expirable = recordReader.getExpirable();

    this.expirableFamily.addExpirable(expirable);
  }

  public RecordReader getRecordReader() {
    return recordReader;
  }

  public void addRecordFilter(RecordFilter recordFilter) {

    recordFilter.setRecordReader(recordReader);
    recordReader = recordFilter;
  }

  public Expirable getExpirable() {
    return expirableFamily;
  }

  public Key getKey() {
    return recordReader.getKey();
  }

  public void addExpirable(Expirable expirable) {
    expirableFamily.addExpirable(expirable);
  }
                                   
  public void execute(ServiceContext context) {
    recordReader.readRecords(context, flow);
  }

  public void setDiscardWriter(RecordWriter discardWriter) {
    //System.out.println(getClass().getName()+".appendToRecordPipeline " + discardWriter.getClass().getName());
    recordReader.setDiscardWriter(discardWriter);
  }
}
                    
