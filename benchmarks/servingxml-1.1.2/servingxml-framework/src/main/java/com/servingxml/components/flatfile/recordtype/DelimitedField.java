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
import com.servingxml.components.label.Label;
import com.servingxml.components.parameter.DefaultValue;
import com.servingxml.app.Flow;
import com.servingxml.util.Formatter;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.components.flatfile.options.FlatFileOptions;

public class DelimitedField implements FlatRecordField {

  private final Name fieldName;
  private final Label label;
  private final int start;
  private final Formatter fieldFormatter;
  private final FlatFileOptions flatFileOptions;
  private final int maxFieldWidth;
  private final DefaultValue defaultValue;

  public DelimitedField(Name fieldName, Label label, int start, int maxFieldWidth, 
                        Formatter fieldFormatter, DefaultValue defaultValue, FlatFileOptions flatFileOptions) {

    this.fieldName = fieldName;
    this.label = label;
    this.start = start;
    this.fieldFormatter = fieldFormatter;
    this.flatFileOptions = flatFileOptions;
    this.maxFieldWidth = maxFieldWidth;
    this.defaultValue = defaultValue;
  }

  public FlatRecordFieldReader createFlatRecordFieldReader() {
    FlatRecordFieldReader reader = new DelimitedFieldReader(fieldName, start, maxFieldWidth, defaultValue, flatFileOptions);
    return reader;
  }

  public FlatRecordFieldWriter createFlatRecordWriter() {

    FlatRecordFieldWriter writer = new DelimitedFieldWriter(fieldName,start,fieldFormatter,
                                                            defaultValue, flatFileOptions);
    return writer;
  }

  public Name getName() {
    return fieldName;          
  }

  public String getLabel(ServiceContext context, Flow flow) {
    return label.getText(context, flow);          
  }

  public boolean isText() {
    return true;
  }

  public boolean isBinary() {
    return false;
  }
}

