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
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.components.flatfile.options.FlatFileOptionsImpl;
import com.servingxml.components.parameter.DefaultValue;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;

public class DelimitedNamedField implements FlatRecordField {

  private final Name nameName;
  private final Name variableName;
  private final FlatRecordField flatRecordField;
  private final int start;
  private final FlatFileOptions flatFileOptions;
  private final DefaultValue defaultValue;

  public DelimitedNamedField(Name nameName, Name variableName, FlatRecordField flatRecordField, int start, 
                              DefaultValue defaultValue, FlatFileOptions flatFileOptions) {

    this.nameName = nameName;
    this.variableName = variableName;
    this.flatRecordField = flatRecordField;
    this.start = start;
    this.flatFileOptions = flatFileOptions;
    this.defaultValue = defaultValue;
  }

  public FlatRecordFieldReader createFlatRecordFieldReader() {
    FlatFileOptionsImpl namedFieldOptions = new FlatFileOptionsImpl(flatFileOptions);
    namedFieldOptions.setFieldDelimiters(flatFileOptions.getNameDelimiters());
    //for (int i = 0; i < flatFileOptions.getNameDelimiters().length; ++i) {
    //System.out.println(flatFileOptions.getNameDelimiters()[i]);
    //}
    //for (int i = 0; i < namedFieldOptions.getNameDelimiters().length; ++i) {
    //System.out.println(namedFieldOptions.getFieldDelimiters()[i]);
    //}
    FlatRecordFieldReader nameReader = new DelimitedFieldReader(nameName, start, -1, defaultValue, namedFieldOptions);
    FlatRecordFieldReader valueReader = flatRecordField.createFlatRecordFieldReader();

    //FlatFileOptionsImpl tagFlatFileOptions = new FlatFileOptionsImpl(flatFileOptions);
    //tagFlatFileOptions.setFieldDelimiters(flatFileOptions.getNameDelimiters());

    FlatRecordFieldReader nameValueReader = new DelimitedNamedFieldReader(nameName, nameReader, variableName,
                                                                           valueReader, flatFileOptions);
    return nameValueReader;
  }

  public FlatRecordFieldWriter createFlatRecordWriter() {
    FlatRecordFieldWriter writer = flatRecordField.createFlatRecordWriter();
    return writer;
  }

  public Name getName() {
    return Name.EMPTY;          
  }

  public String getLabel(ServiceContext context, Flow flow) {
    return flatRecordField.getLabel(context, flow);          
  }

  public boolean isText() {
    return flatRecordField.isText();
  }

  public boolean isBinary() {
    return flatRecordField.isBinary();
  }
}

