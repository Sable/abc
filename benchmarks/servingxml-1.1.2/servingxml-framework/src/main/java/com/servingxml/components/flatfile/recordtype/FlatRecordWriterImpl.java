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

package com.servingxml.components.flatfile.recordtype;

import com.servingxml.app.ServiceContext;
import com.servingxml.util.record.Record;
import com.servingxml.app.Flow;
import com.servingxml.components.flatfile.FlatRecordWriter;
import com.servingxml.components.flatfile.RecordOutput;

/**
 * The <code>FlatRecordWriterImpl</code> object implements a FlatRecordWriter
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FlatRecordWriterImpl implements FlatRecordWriter {
  private final FlatRecordFieldWriter[] fieldWriters;
  private final boolean omitFinalFieldDelimiter;
  
  public FlatRecordWriterImpl(boolean omitFinalFieldDelimiter, FlatRecordFieldWriter[] fieldWriters) {
    this.omitFinalFieldDelimiter = omitFinalFieldDelimiter;
    this.fieldWriters = fieldWriters;
  }

  public void writeRecord(ServiceContext context, Flow flow, RecordOutput recordOutput) {
    Record parameters = flow.getParameters();
    int last = fieldWriters.length - 1;
    //System.out.println(getClass().getName()+".writeRecord fields="+fieldWriters.length);
    for (int i = 0; i < fieldWriters.length;  ++i) {
      FlatRecordFieldWriter fieldWriter = fieldWriters[i]; 
      fieldWriter.writeField(context, flow, recordOutput);
      if (omitFinalFieldDelimiter && i != last) {
        //System.out.println(getClass().getName()+".writeRecord putting delimiter " + i + ", fieldWriter="+fieldWriter.getClass().getName());
        fieldWriter.writeEndDelimiterTo(recordOutput);
      }
    }
    //  The record writer must position itself at the end of the record
    recordOutput.setPosition(recordOutput.getSize());
  }

  public FlatRecordWriter resolveFlatRecordWriter(ServiceContext context, 
    Flow flow) {
    return this;
  }
}
