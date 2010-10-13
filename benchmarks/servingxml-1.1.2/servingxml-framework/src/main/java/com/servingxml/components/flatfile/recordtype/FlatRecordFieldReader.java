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
import com.servingxml.util.record.Record;
import com.servingxml.app.ServiceContext;      
import com.servingxml.util.record.Value;
import com.servingxml.app.Flow;
import com.servingxml.components.flatfile.RecordInput;
import com.servingxml.components.flatfile.options.Delimiter;
import com.servingxml.components.flatfile.options.DelimiterExtractor;

public interface FlatRecordFieldReader {
  void readField(ServiceContext context, 
                 Flow flow,
                 RecordInput recordInput, 
                 DelimiterExtractor[] recordDelimiters, 
                 int recordDelimiterStart, 
                 int recordDelimiterCount, 
                 int maxRecordWidth,
                 RecordBuilder recordBuilder);

  /**
   * Returns the end position of the field, relative to the start 
   * position, if known, otherwise the current position. Returns 
   * -1 if the width of the field is unknown, for example, if the 
   * field is ended with a delimiter, or if the field contains an 
   * unknown count of repeating fixed length records. 
   */

  int getFixedEndPosition(Record parameters, Record currentRecord, int currentPosition);
}
