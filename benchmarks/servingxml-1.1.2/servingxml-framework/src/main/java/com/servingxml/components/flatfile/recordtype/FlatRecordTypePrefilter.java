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
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.flatfile.FlatRecordReader;
import com.servingxml.components.flatfile.FlatRecordWriter;

/**
 * The <code>FlatRecordTypePrefilter</code> object writes a header line
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FlatRecordTypePrefilter implements FlatRecordType {
  private final FlatRecordType flatRecordType;
  private final ParameterDescriptor[] parameterDescriptors;

  public FlatRecordTypePrefilter(FlatRecordType flatRecordType, 
    ParameterDescriptor[] parameterDescriptors) {
    this.flatRecordType = flatRecordType;
    this.parameterDescriptors = parameterDescriptors;
  }

  public Record getDefaultRecord(ServiceContext context, Flow flow) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    return flatRecordType.getDefaultRecord(context, newFlow);
  }

  public FlatRecordReader createFlatRecordReader() {
    FlatRecordReader flatRecordReader = flatRecordType.createFlatRecordReader();
    return new FlatRecordReaderPrefilter(flatRecordReader, parameterDescriptors); 
  }

  public FlatRecordWriter createFlatRecordWriter() {
    FlatRecordWriter flatRecordWriter = flatRecordType.createFlatRecordWriter();
    return new FlatRecordWriterPrefilter(flatRecordWriter, parameterDescriptors); 
  }

  public boolean isText() {
    return flatRecordType.isText();
  }

  public boolean isBinary() {
    return flatRecordType.isBinary();
  }

  public boolean isFixedLength() {
    return flatRecordType.isFixedLength();
  }
}
