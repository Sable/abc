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
import com.servingxml.io.cache.DefaultKey;

public final class RecordWriterFilterAdaptor extends AbstractRecordFilter implements RecordFilter {
  private RecordWriter recordWriter = RecordWriter.NULL;

  public RecordWriterFilterAdaptor(RecordWriter recordWriter) {
    super.setRecordWriter(recordWriter);
  }

  public final void readRecords(ServiceContext context, Flow flow) 
  {
    super.readRecords(context, flow);
  }

  public final Key getKey() {
    return super.getKey();
  }

  public final Expirable getExpirable() {
    return super.getExpirable(); 
  }

  public final void setRecordWriter(RecordWriter recordWriter) {
    //System.out.println(getClass().getName()+".setRecordWriter "+ recordWriter.getClass().getName());
    this.recordWriter = recordWriter;
  }

  public final void startRecordStream(ServiceContext context, Flow flow) 
  {
    recordWriter.startRecordStream(context, flow);
    super.startRecordStream(context, flow);
  }

  public final void endRecordStream(ServiceContext context, Flow flow) 
  {
    recordWriter.endRecordStream(context, flow);
    super.endRecordStream(context, flow);
  }

  public final void writeRecord(ServiceContext context, Flow flow) 
  {
    super.writeRecord(context, flow);
    recordWriter.writeRecord(context, flow);
    // A writer terminates the record stream
  }

  public final void close() {
    ServingXmlException badDispose = null;
    try {
      super.close();
    } catch (ServingXmlException e) {
      badDispose = e;
    } catch (Exception e) {
      badDispose = new ServingXmlException(e.getMessage(),e);
    }
    try {
      recordWriter.close();      
    } catch (ServingXmlException e) {
      badDispose = e;
    } catch (Exception e) {
      badDispose = new ServingXmlException(e.getMessage(),e);
    }
    if (badDispose != null) {
      throw badDispose;
    }
  }
}
