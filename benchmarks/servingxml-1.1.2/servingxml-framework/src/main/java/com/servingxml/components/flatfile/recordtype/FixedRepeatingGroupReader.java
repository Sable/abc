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
import com.servingxml.components.flatfile.options.Delimiter;
import com.servingxml.components.flatfile.options.DelimiterExtractor;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.expr.substitution.IntegerSubstitutionExpr;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordBuilder;
import com.servingxml.util.record.RecordReceiver;

public class FixedRepeatingGroupReader implements FlatRecordFieldReader {
  private final FlatFileOptions flatFileOptions;
  private final Name fieldName;
  private final IntegerSubstitutionExpr startExpr;
  private final FlatRecordReader segmentReader;
  private final IntegerSubstitutionExpr countExpr;

  public FixedRepeatingGroupReader(FlatFileOptions flatFileOptions, 
                                   Name fieldName, 
                                   IntegerSubstitutionExpr startExpr, 
                                   FlatRecordReader segmentReader, 
                                   IntegerSubstitutionExpr countExpr) {
    //System.out.println(getClass().getName()+".cons fieldName = " + fieldName);

    this.flatFileOptions = flatFileOptions;
    this.fieldName = fieldName;
    this.startExpr = startExpr;
    this.segmentReader = segmentReader;
    this.countExpr = countExpr;
  }

  public void readField(final ServiceContext context, 
                        Flow flow, 
                        RecordInput recordInput, 
                        DelimiterExtractor[] recordDelimiters, 
                        int recordDelimiterStart, 
                        int recordDelimiterCount, 
                        int maxRecordWidth,
                        RecordBuilder recordBuilder) {

    try {
      Record parameters = flow.getParameters();
      int count = countExpr.evaluateAsInt(parameters,recordBuilder);

      //System.out.println(getClass().getName()+".readField count="+count);

      final List<Record> segmentList = new ArrayList<Record>();
      RecordReceiver segmentReceiver = new RecordReceiver() {
        public void receiveRecord(Record segment) {
          //System.out.println(getClass().getName()+".readField segment received \n  " + segment.toXmlString(context));
          segmentList.add(segment);
        }
      };

      int offset = flatFileOptions.rebaseIndex(startExpr.evaluateAsInt(parameters,recordBuilder));
      if (offset >= 0) {
        recordInput.setPosition(offset);
      }
      //System.out.println(getClass().getName()+".readField start="+(offset+1)+",count="+count);

      //System.out.println(getClass().getName()+".readField count=" + count);
      Flow newFlow = flow.replaceRecord(context,recordBuilder);
      for (int i = 0; !recordInput.done() && i < count; ++i) {
        segmentReader.readRecord(context, newFlow, recordInput, recordDelimiters, recordDelimiterStart, recordDelimiterCount, 
                                 maxRecordWidth, segmentReceiver);
      }
      //System.out.println(getClass().getName()+".readField done read");

      if (segmentList.size() > 0) {
        Record[] segments = new Record[segmentList.size()];
        segments = segmentList.toArray(segments);

        recordBuilder.setRecords(fieldName, segments);
      }
    } catch (ServingXmlException e) {
      throw e;
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(), e);
    }

  }

  public int getFixedEndPosition(Record parameters, Record currentRecord, int currentPosition) {
    return -1;
  }
}
