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
import com.servingxml.app.Flow;
import com.servingxml.components.flatfile.FlatRecordReader;
import com.servingxml.components.flatfile.FlatRecordWriter;

/**
 * The <code>FlatRecordType</code> object writes a header line
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class VbsFlatRecordType implements FlatRecordType {
  private final FlatRecordType[] flatRecordTypes;

  public VbsFlatRecordType(FlatRecordType[] flatRecordTypes) {
    this.flatRecordTypes = flatRecordTypes;
  }

  public Record getDefaultRecord(ServiceContext context, Flow flow) {
    return Record.EMPTY;
  }

  public FlatRecordReader createFlatRecordReader() {
    FlatRecordReader[] flatRecordReaders = new FlatRecordReader[flatRecordTypes.length];
    for (int i = 0; i < flatRecordTypes.length; ++i) {
      flatRecordReaders[i] = flatRecordTypes[i].createFlatRecordReader();
    }
    return new VbsFlatRecordReader(flatRecordReaders);
  }

  public FlatRecordWriter createFlatRecordWriter() {
    return flatRecordTypes[0].createFlatRecordWriter();
  }

  public boolean isText() {
    boolean result = true;
    for (int i = 0; result && i < flatRecordTypes.length; ++i) {
      if (!flatRecordTypes[i].isText()) {
        result = false;
      }
    }

    return result;
  }

  public boolean isBinary() {
    boolean result = true;
    for (int i = 0; result && i < flatRecordTypes.length; ++i) {
      if (!flatRecordTypes[i].isBinary()) {
        result = false;
      }
    }

    return result;
  }

  public boolean isFixedLength() {
    boolean result = true;
    for (int i = 0; result && i < flatRecordTypes.length; ++i) {
      if (!flatRecordTypes[i].isFixedLength()) {
        result = false;
      }
    }

    return result;
  }
}
