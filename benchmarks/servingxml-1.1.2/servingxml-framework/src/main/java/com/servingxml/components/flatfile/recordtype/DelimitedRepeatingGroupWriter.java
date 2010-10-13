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

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.flatfile.FlatRecordWriter;
import com.servingxml.components.flatfile.RecordOutput;
import com.servingxml.components.flatfile.options.Delimiter;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.util.Name;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.Value;
import com.servingxml.expr.substitution.IntegerSubstitutionExpr;

public class DelimitedRepeatingGroupWriter implements FlatRecordFieldWriter {
  private final Name fieldName;
  private final IntegerSubstitutionExpr startExpr;
  private final FlatRecordWriter segmentWriter;
  private final Delimiter repeatDelimiter;
  private final Delimiter segmentDelimiter;
  private final boolean omitFinalRepeatDelimiter;
  private final FlatFileOptions flatFileOptions;

  public DelimitedRepeatingGroupWriter(Name fieldName, IntegerSubstitutionExpr startExpr, 
    FlatRecordWriter segmentWriter, FlatFileOptions flatFileOptions) {
    this.fieldName = fieldName;
    this.startExpr = startExpr;
    this.segmentWriter = segmentWriter;
    this.omitFinalRepeatDelimiter = flatFileOptions.isOmitFinalRepeatDelimiter();
    Delimiter[] repeatDelimiters = flatFileOptions.getRepeatDelimiters();
    this.repeatDelimiter = repeatDelimiters.length == 0 ? Delimiter.NULL : repeatDelimiters[0];
    Delimiter[] segmentDelimiters = flatFileOptions.getSegmentDelimiters();
    this.segmentDelimiter = segmentDelimiters.length == 0 ? Delimiter.NULL : segmentDelimiters[0];
    this.flatFileOptions = flatFileOptions;
  }

  public void writeField(ServiceContext context, Flow flow, RecordOutput recordOutput) {
    writeField(context, flow, fieldName, recordOutput);
  }

  public void writeField(ServiceContext context, Flow flow, Name fieldName, RecordOutput recordOutput) {
    int start = startExpr.evaluateAsInt(flow.getParameters(),flow.getRecord());
    int offset = flatFileOptions.rebaseIndex(start);
    if (offset >= 0) {
      recordOutput.setPosition(offset);
    }
    Record record = flow.getRecord();
    Value value = record.getValue(fieldName);
   //System.out.println(record.toXmlString(context));
    if (value != null) {
      Record[] records = value.getRecords();
      //System.out.println(getClass().getName()+".writeField records="+records.length);
      for (int i = 0; i < records.length; ++i) {
        Record segment = records[i];
        Flow newFlow = flow.replaceRecord(context, segment);
        segmentWriter.writeRecord(context, newFlow, recordOutput);
        if (omitFinalRepeatDelimiter && i != records.length-1) {
          //System.out.println(getClass().getName()+".writeField putting delimiter " + i);
          repeatDelimiter.writeEndDelimiterTo(recordOutput);
        }
      }
    }
  }

  public void writeEndDelimiterTo(RecordOutput recordOutput) {
    segmentDelimiter.writeEndDelimiterTo(recordOutput);
  }
}
