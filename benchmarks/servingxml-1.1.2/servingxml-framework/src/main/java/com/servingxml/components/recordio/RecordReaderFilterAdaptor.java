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
import com.servingxml.util.ServingXmlException;
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.Key;
import com.servingxml.app.Flow;

public class RecordReaderFilterAdaptor extends AbstractRecordFilter implements RecordFilter {
  private final RecordReader myRecordReader;
  
  public RecordReaderFilterAdaptor(RecordReader myRecordReader) {            
    this.myRecordReader = myRecordReader;
  }
                                             
  public void writeRecord(ServiceContext context, Flow flow) {
    myRecordReader.setRecordWriter(new MyRecordWriter(getRecordWriter()));
    myRecordReader.readRecords(context, flow);
  }
}

              
class MyRecordWriter implements RecordWriter {
  private final RecordWriter recordWriter;

  MyRecordWriter(RecordWriter recordWriter) {
    this.recordWriter = recordWriter;                  
  }

  public void startRecordStream(ServiceContext context, Flow flow) {
  }
  public void endRecordStream(ServiceContext context, Flow flow) {
  }
  public void writeRecord(ServiceContext context, Flow flow) {
    recordWriter.writeRecord(context, flow);
  }
  public void close() {
  }
}

