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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.flatfile.FlatRecordReader;
import com.servingxml.components.flatfile.RecordInput;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.expr.substitution.IntegerSubstitutionExpr;
import com.servingxml.util.ByteArrayBuilder;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordBuilder;
import com.servingxml.util.record.RecordReceiver;
import com.servingxml.components.flatfile.options.Delimiter;
import com.servingxml.components.flatfile.options.DelimiterExtractor;

public class DelimitedRepeatingGroupReader implements FlatRecordFieldReader {
  private final Name fieldName;
  private final IntegerSubstitutionExpr startExpr;
  private final FlatRecordReader flatRecordReader;
  private final ByteArrayBuilder byteArrayBuilder;
  private final IntegerSubstitutionExpr countExpr;
  private final FlatFileOptions flatFileOptions;
  private int level = 0;
  private int index = 0;

  public DelimitedRepeatingGroupReader(Name fieldName, 
                                       IntegerSubstitutionExpr startExpr, 
                                       FlatRecordReader flatRecordReader, 
                                       IntegerSubstitutionExpr countExpr, 
                                       FlatFileOptions flatFileOptions) {
    this.fieldName = fieldName;
    this.startExpr = startExpr;
    this.flatRecordReader = flatRecordReader;
    this.flatFileOptions = flatFileOptions;
    this.byteArrayBuilder = new ByteArrayBuilder();
    this.countExpr = countExpr;
  }

  public void readField(final ServiceContext context, 
                        Flow flow,
                        final RecordInput recordInput, 
                        DelimiterExtractor[] recordDelimiters, 
                        int recordDelimiterStart, 
                        int recordDelimiterCount, 
                        int maxRecordWidth,
                        RecordBuilder recordBuilder) {

    Record parameters = flow.getParameters();
    try {
      int offset = flatFileOptions.rebaseIndex(startExpr.evaluateAsInt(parameters,recordBuilder));
      if (offset >= 0) {
        recordInput.setPosition(offset);
      }
    } catch (IOException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }

    int count = countExpr.evaluateAsInt(parameters,recordBuilder);

    final List<Record> recordList = new ArrayList<Record>();
    RecordReceiver recordReceiver = new RecordReceiver() {
      public void receiveRecord(Record record) {
        recordList.add(record);
      }
    };

    Flow newFlow = flow.replaceRecord(context,recordBuilder);
    recordInput.readRepeatingGroup2(context, newFlow, count, flatFileOptions, 
                                   recordDelimiters, 
                                   recordDelimiterStart, 
                                   recordDelimiterCount,
                                   maxRecordWidth,
                                   flatRecordReader, recordReceiver);

    if (recordList.size() > 0) {
      //System.out.println(getClass().getName()+".readField setting field "+counter);
      Record[] segments = new Record[recordList.size()];
      segments = recordList.toArray(segments);
      recordBuilder.setRecords(fieldName, segments);
    }
  }

  public int getFixedEndPosition(Record parameters, Record currentRecord, int currentPosition) {
    return -1;
  }
}
