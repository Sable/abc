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
import com.servingxml.app.Flow;
import com.servingxml.util.Name;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.Value;
import com.servingxml.components.flatfile.FlatRecordWriter;
import com.servingxml.components.flatfile.RecordOutput;
import com.servingxml.expr.substitution.IntegerSubstitutionExpr;

public class FixedRepeatingGroupWriter implements FlatRecordFieldWriter {
  private final Name fieldName;
  private final IntegerSubstitutionExpr startExpr;
  private final FlatRecordWriter segmentWriter;

  public FixedRepeatingGroupWriter(Name fieldName, IntegerSubstitutionExpr startExpr, FlatRecordWriter segmentWriter) {
    this.fieldName = fieldName;
    this.startExpr = startExpr;
    this.segmentWriter = segmentWriter;
  }

  public void writeField(ServiceContext context, Flow flow, RecordOutput recordOutput) {
    writeField(context, flow, fieldName, recordOutput);
  }

  public void writeField(ServiceContext context, Flow flow, Name fieldName, RecordOutput recordOutput) {
    Record record = flow.getRecord();
    Value value = record.getValue(fieldName);
    //System.out.println(getClass().getName()+".writeField " + record.toXmlString(context));
    if (value != null) {
      Record[] records = value.getRecords();
      for (int i = 0; i < records.length; ++i) {
        Record segment = records[i];
        Flow newFlow = flow.replaceRecord(context, segment);
        //System.out.println(getClass().getName()+".writeField " + segmentWriter.getClass().getName() + "\n" + segment.toXmlString(context));
        segmentWriter.writeRecord(context,newFlow,recordOutput);
      }
    }
  }

  public void writeEndDelimiterTo(RecordOutput recordOutput) {
  }
}
