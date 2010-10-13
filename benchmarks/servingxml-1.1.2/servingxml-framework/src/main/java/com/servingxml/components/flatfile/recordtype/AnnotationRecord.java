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
import com.servingxml.util.Name;
import com.servingxml.app.Flow;
import com.servingxml.components.flatfile.FlatRecordReader;
import com.servingxml.components.flatfile.FlatRecordWriter;

/**
 * The <code>AnnotationRecord</code> object writes a header line
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class AnnotationRecord implements FlatRecordType {
  private final String value;
  private final int width;
  private final int recordLength;
  
  public AnnotationRecord(String value, int width, int recordLength) {
    this.value = value;
    this.width = width;
    this.recordLength = recordLength;
  }
  
  public void exportLine(StringBuilder buf) {
    buf.append(value);
  }

  public Record getDefaultRecord(ServiceContext context, Flow flow) {
    return Record.EMPTY;
  }

  public Name getName() {
    return Name.EMPTY;
  }

  public FlatRecordReader createFlatRecordReader() {
    int length = isFixedLength() ? recordLength : width;
    return new AnnotationRecordReader(length);
  }

  public FlatRecordWriter createFlatRecordWriter() {
    return new AnnotationRecordWriter(value);
  }

  public boolean isText() {
    return true;
  }

  public boolean isBinary() {
    return false;
  }

  public boolean isFixedLength() {
    return recordLength >= 0;
  }

}
