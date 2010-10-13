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

public interface RecordReader {
  public static final RecordReader NULL = new NullRecordReader();

  void readRecords(ServiceContext context, Flow flow);

  Key getKey();

  Expirable getExpirable();

  RecordWriter getRecordWriter();

  void setRecordWriter(RecordWriter recordWriter);

  RecordWriter getDiscardWriter();

  void setDiscardWriter(RecordWriter discardWriter);

  void discardRecord(ServiceContext context, Flow flow, ServingXmlException e);

  static class NullRecordReader implements RecordReader {
    private static final Key key = DefaultKey.newInstance();

    public final void readRecords(ServiceContext context, Flow flow) {
    }

    public final Key getKey() {
      return key;
    }

    public final Expirable getExpirable() {
      return Expirable.IMMEDIATE_EXPIRY;
    }

    public final RecordWriter getRecordWriter() {
      return RecordWriter.NULL;
    }

    public final void setRecordWriter(RecordWriter writer) {
    }

    public final RecordWriter getDiscardWriter() {
      return RecordWriter.NULL;
    }

    public final void setDiscardWriter(RecordWriter writer) {
    }

    public void discardRecord(ServiceContext context, Flow flow, ServingXmlException e) {
    }
  }
}                                         



