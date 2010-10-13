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

import java.util.List;

import com.servingxml.util.LineFormatter;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactory;

/**
 * The <code>AnnotationRecord</code> object writes a header line
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class AnnotationRecordFactory implements FlatRecordTypeFactory {
  private final String value;
  private final int width;
  private final int recordLength;
  private final FlatFileOptionsFactory flatFileOptionsFactory;
  
  public AnnotationRecordFactory(String value, int width, int recordLength, 
                             FlatFileOptionsFactory flatFileOptionsFactory) {
    this.value = value;
    this.width = width;
    this.recordLength = recordLength;
    this.flatFileOptionsFactory = flatFileOptionsFactory;
  }                                       

  public FlatRecordType createFlatRecordType(ServiceContext context, Flow flow, FlatFileOptions defaultOptions) {
    FlatFileOptions flatFileOptions = flatFileOptionsFactory.createFlatFileOptions(context, flow, defaultOptions);

    LineFormatter lineFormatter = new LineFormatter(width,defaultOptions.getAlignment(),
      defaultOptions.getPadCharacter());
    String line = lineFormatter.format(value);
    int len = width;
    if (len == -1) {
      len = line.length();
    }

    FlatRecordType recordType = new AnnotationRecord(line, len, recordLength);
    return recordType;
  }

  public void appendFlatRecordField(ServiceContext context, Flow flow,
    FlatFileOptions defaultOptions, List<FlatRecordField> flatRecordFieldList) {
  }

  public boolean isFieldDelimited() {
    return false;
  }

  public boolean isBinary() {
    return false;
  }

  public boolean isText() {
    return true;
  }
}
