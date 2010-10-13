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

package com.servingxml.components.flatfile;

import com.servingxml.app.ServiceContext;
import com.servingxml.util.record.RecordReceiver;
import com.servingxml.app.Flow;
import com.servingxml.util.record.Record;
import com.servingxml.components.flatfile.options.Delimiter;
import com.servingxml.components.flatfile.options.DelimiterExtractor;

public interface FlatRecordReader {
  public static final FlatRecordReader NULL = new NullFlatRecordReader();

  int calculateFixedRecordLength(Record parameters, Record currentRecord);

  void readRecord(ServiceContext context, 
                  Flow flow,
                  RecordInput recordInput, 
                  DelimiterExtractor[] recordDelimiters,
                  int recordDelimiterStart, 
                  int recordDelimiterCount, 
                  int maxRecordWidth,
                  RecordReceiver receiver);

  void endReadRecords(ServiceContext context, 
                      Flow flow, 
                      final DelimiterExtractor[] recordDelimiters,
                      final int recordDelimiterStart,
                      final int recordDelimiterCount, 
                      int maxRecordWidth,
                      RecordReceiver receiver);

  static final class NullFlatRecordReader implements FlatRecordReader {
    public void readRecord(ServiceContext context, 
                           Flow flow,
                           RecordInput recordInput, 
                           DelimiterExtractor[] recordDelimiters,
                           int recordDelimiterStart,
                           int recordDelimiterCount, 
                           int maxRecordWidth,
                           RecordReceiver receiver) {
    }

    public int calculateFixedRecordLength(Record parameters, Record currentRecord) {
      return 0;
    }

    public void endReadRecords(ServiceContext context, Flow flow, 
                               final DelimiterExtractor[] recordDelimiters,
                               final int recordDelimiterStart,
                               final int recordDelimiterCount, 
                               int maxRecordWidth,
                               RecordReceiver receiver) {
    }
  }
}


