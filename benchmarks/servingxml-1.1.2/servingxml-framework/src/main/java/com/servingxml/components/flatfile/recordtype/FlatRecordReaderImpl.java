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

import com.servingxml.util.record.RecordBuilder;
import com.servingxml.app.ServiceContext;
import com.servingxml.util.record.Record;
import com.servingxml.util.Name;
import com.servingxml.util.record.RecordReceiver;
import com.servingxml.components.common.NameSubstitutionExpr;
import com.servingxml.app.Flow;
import com.servingxml.components.flatfile.RecordInput;
import com.servingxml.components.flatfile.FlatRecordReader;
import com.servingxml.expr.substitution.IntegerSubstitutionExpr;
import com.servingxml.components.flatfile.options.Delimiter;
import com.servingxml.components.flatfile.options.DelimiterExtractor;

public class FlatRecordReaderImpl implements FlatRecordReader {
  private final NameSubstitutionExpr recordTypeNameExpr;
  private final FlatRecordFieldReader[] fieldReaders;
  private final IntegerSubstitutionExpr recordLengthExpr;

  public FlatRecordReaderImpl(NameSubstitutionExpr recordTypeNameExpr, 
                              FlatRecordFieldReader[] fieldReaders, 
                              IntegerSubstitutionExpr recordLengthExpr) {
    this.recordTypeNameExpr = recordTypeNameExpr;
    this.fieldReaders = fieldReaders;
    this.recordLengthExpr = recordLengthExpr;
  }

  public void readRecord(ServiceContext context, 
                         Flow flow,
                         RecordInput recordInput, 
                         final DelimiterExtractor[] recordDelimiters,
                         final int recordDelimiterStart,
                         final int recordDelimiterCount, 
                         int maxRecordWidth,
                         RecordReceiver receiver) {

    Name recordTypeName = recordTypeNameExpr.evaluateName(flow.getParameters(), flow.getRecord());
    if (!recordLengthExpr.isNull()) {
      int recordLength = recordLengthExpr.evaluateAsInt(flow.getParameters(), flow.getRecord());
      recordInput = recordInput.readSegment(recordLength);
      //System.out.println(getClass().getName()+".readRecord recordTypeName=" + recordTypeName + ", recordLength=" + recordLength + " " + flow.getRecord().toXmlString(context.getAppContext().getResources().getQnameContext().getPrefixMap()));
    }
    //System.out.println (getClass().getName()+".readRecord enter index = " + offset + " |" + new String(data, start, length)+"|");
    //System.out.println (getClass().getName()+".readRecord " + flow.getRecord().toXmlString(context));

    //System.out.println(getClass().getName()+".readRecord recordType="+recordTypeName);
    //System.out.println(getClass().getName()+".readRecord recordType=" + recordTypeName);

    RecordBuilder recordBuilder = new RecordBuilder(recordTypeName);
    for (int i = 0; i < fieldReaders.length; ++i) {
      //System.out.println(getClass().getName()+".readRecord before read " + i);
      FlatRecordFieldReader fieldReader = fieldReaders[i];
      fieldReader.readField(context, flow, recordInput, recordDelimiters, recordDelimiterStart, recordDelimiterCount, 
                            maxRecordWidth, recordBuilder);
      //System.out.println(getClass().getName()+".readRecord after read ");
    }
    //System.out.println (getClass().getName()+".readRecord index="+index);
    Record record = recordBuilder.toRecord();
    receiver.receiveRecord(record);
    //System.out.println (getClass().getName()+".readRecord leave");
  }

  public int calculateFixedRecordLength(Record parameters, Record currentRecord) {
    int length;

    if (!recordLengthExpr.isNull()) {
      //System.out.println(getClass().getName()+".calculateFixedRecordLength record="+currentRecord);
      length = recordLengthExpr.evaluateAsInt(parameters,currentRecord);
    } else {
      length = 0;
      for (int i = 0; i < fieldReaders.length; ++i) {
        int pos = fieldReaders[i].getFixedEndPosition(parameters, currentRecord, length);
        if (pos >= length) {
          length = pos;
        }
      }
    }
    return length;
  }

  public void endReadRecords(final ServiceContext context, 
                             final Flow flow, 
                             final DelimiterExtractor[] recordDelimiters,
                             final int recordDelimiterStart, 
                             int recordDelimiterCount, 
                             int maxRecordWidth,
                             final RecordReceiver receiver) {
  }
}
