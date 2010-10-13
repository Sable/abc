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
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.flatfile.FlatRecordReader;
import com.servingxml.components.flatfile.RecordInput;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordReceiver;
import com.servingxml.components.flatfile.options.Delimiter;
import com.servingxml.components.flatfile.options.DelimiterExtractor;

public class FlatRecordReaderPrefilter implements FlatRecordReader {
  private final FlatRecordReader flatRecordReader;
  private final ParameterDescriptor[] parameterDescriptors;

  public FlatRecordReaderPrefilter(FlatRecordReader flatRecordReader,
                                   ParameterDescriptor[] parameterDescriptors) {
    this.flatRecordReader = flatRecordReader;
    this.parameterDescriptors = parameterDescriptors;
  }

  public void readRecord(ServiceContext context, 
                         Flow flow,
                         RecordInput recordInput, 
                         final DelimiterExtractor[] recordDelimiters,
                         final int recordDelimiterStart,
                         final int recordDelimiterCount, 
                         int maxRecordWidth,
                         RecordReceiver receiver) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    //System.out.println(getClass().getName()+"readRecord parameters="+newParameters.toXmlString(context));
    flatRecordReader.readRecord(context,newFlow,recordInput, recordDelimiters, recordDelimiterStart, recordDelimiterCount,
                                maxRecordWidth, receiver);
  }

  public int calculateFixedRecordLength(Record parameters, Record currentRecord) {
    //ToDo:  revisit
    return flatRecordReader.calculateFixedRecordLength(parameters, currentRecord);
  }

  public void endReadRecords(final ServiceContext context, 
                             final Flow flow, 
                             final DelimiterExtractor[] recordDelimiters,
                             final int recordDelimiterStart, 
                             int recordDelimiterCount, 
                             int maxRecordWidth,
                             final RecordReceiver receiver) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    flatRecordReader.endReadRecords(context, newFlow, recordDelimiters, recordDelimiterStart, recordDelimiterCount, 
                                    maxRecordWidth, receiver);
  }
}
