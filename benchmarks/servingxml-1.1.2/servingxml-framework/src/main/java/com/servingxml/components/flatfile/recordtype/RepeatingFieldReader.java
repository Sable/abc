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
import com.servingxml.components.flatfile.RecordInput;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.expr.substitution.IntegerSubstitutionExpr;
import com.servingxml.util.ByteArrayBuilder;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.RecordBuilder;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.Value;
import com.servingxml.components.flatfile.options.Delimiter;
import com.servingxml.components.flatfile.options.DelimiterExtractor;

public class RepeatingFieldReader implements FlatRecordFieldReader {
  private final FlatRecordFieldReader fieldReader;
  private final FlatFileOptions flatFileOptions;
  private final IntegerSubstitutionExpr countExpr;
  private final ByteArrayBuilder byteArrayBuilder;

  public RepeatingFieldReader(FlatRecordFieldReader fieldReader, 
                              IntegerSubstitutionExpr countExpr, 
                              FlatFileOptions flatFileOptions) {
    this.fieldReader = fieldReader;
    this.flatFileOptions = flatFileOptions;
    this.countExpr = countExpr;
    this.byteArrayBuilder = new ByteArrayBuilder();
  }

  public void readField(ServiceContext context, 
                        Flow flow, 
                        final RecordInput recordInput, 
                        DelimiterExtractor[] recordDelimiters, 
                        int recordDelimiterStart, 
                        int recordDelimiterCount, 
                        int maxRecordWidth,
                        RecordBuilder recordBuilder) {
    //System.out.println (getClass().getName()+".readField enter index = " + position + " |" + new String(data, start, length)+"|");

    if (flatFileOptions.getSegmentDelimiters().length == 0) {
      readAllFields(context, flow, recordInput, recordDelimiters, recordDelimiterStart, recordDelimiterCount, maxRecordWidth, recordBuilder);
    } else {
      byteArrayBuilder.clear();
      RecordInput segment = recordInput.readSegment(flatFileOptions);
      readAllFields(context, flow, segment, recordDelimiters, recordDelimiterStart, recordDelimiterCount, maxRecordWidth, recordBuilder);
    }
  }

  public void readAllFields(ServiceContext context, 
                            Flow flow,
                            RecordInput recordInput, 
                            DelimiterExtractor[] recordDelimiters,
                            final int recordDelimiterStart, 
                            int recordDelimiterCount, 
                            int maxRecordWidth,
                            RecordBuilder recordBuilder) {

    //System.out.println(getClass().getName()+".readAllFields enter");
    //System.out.println(getClass().getName()+".readField " + recordInput.toString());

    int count = countExpr.evaluateAsInt(flow.getParameters(),recordBuilder);

    for (int i=0; !recordInput.done() && i < count; ++i) {
      fieldReader.readField(context, flow, recordInput, recordDelimiters, recordDelimiterStart, recordDelimiterCount, 
                            maxRecordWidth, recordBuilder);
    }
  }

  public int getFixedEndPosition(Record parameters, Record currentRecord, int currentPosition) {
    return -1;
  }
}

