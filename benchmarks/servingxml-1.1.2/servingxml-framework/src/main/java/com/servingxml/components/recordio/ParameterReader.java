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
import com.servingxml.util.Name;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordBuilder;
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.Key;
import com.servingxml.io.cache.DefaultKey;
import com.servingxml.components.recordio.AbstractRecordReader;

/**
 * A <code>ParameterReader</code> implements a <code>RecordReader</code> interface.
 *
 * 
 * @author  Daniel A. Parker
 */

public class ParameterReader extends AbstractRecordReader implements RecordReader {

  private final Name recordTypeName;
  private final Key key;

  public ParameterReader(Name recordTypeName) {

    this.recordTypeName = recordTypeName;
    this.key = DefaultKey.newInstance();
  }

  public Key getKey() {
    return key;         
  }

  public Expirable getExpirable() {
    return Expirable.IMMEDIATE_EXPIRY;
  }

  public void readRecords(ServiceContext context, Flow flow) {
    try {
      startRecordStream(context,flow);
      RecordBuilder builder = new RecordBuilder(recordTypeName,flow.getParameters());
      Record record = builder.toRecord();
      Flow newFlow = flow.replaceRecord(context, record);
      getRecordWriter().writeRecord(context, newFlow);
      endRecordStream(context,flow);
    } finally {
      try {
        close();
      } catch (Exception e) {
        //  Don't care
      }
    }
  }
}

