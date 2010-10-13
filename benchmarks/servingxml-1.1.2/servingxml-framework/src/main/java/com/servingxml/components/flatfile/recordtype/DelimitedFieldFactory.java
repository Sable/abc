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

import com.servingxml.util.Name;
import com.servingxml.util.Formatter;
import com.servingxml.components.parameter.DefaultValue;
import com.servingxml.components.label.Label;
import com.servingxml.util.LineFormatter;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactory;

public class DelimitedFieldFactory implements FlatRecordFieldFactory {

  private final Name name;
  private final Label label;
  private final int offset;
  private final int minLength;
  private final int maxLength;
  private final FlatFileOptionsFactory flatFileOptionsFactory;
  private final int maxFieldWidth;
  private final DefaultValue defaultValueEvaluator;

  public DelimitedFieldFactory(Name name, Label label, int offset, int maxFieldWidth, 
  int minLength, int maxLength, DefaultValue defaultValueEvaluator, FlatFileOptionsFactory flatFileOptionsFactory) {

    this.name = name;
    this.label = label;
    this.offset = offset;
    this.minLength = minLength;
    this.maxLength = maxLength;
    this.flatFileOptionsFactory = flatFileOptionsFactory;
    this.maxFieldWidth = maxFieldWidth;
    this.defaultValueEvaluator = defaultValueEvaluator;
  }

  public void appendFlatRecordField(ServiceContext context, Flow flow,
    FlatFileOptions defaultOptions, List<FlatRecordField> flatRecordFieldList) {
    FlatFileOptions flatFileOptions = flatFileOptionsFactory.createFlatFileOptions(context, flow, defaultOptions);

    Formatter fieldFormatter = (minLength == 0 && maxLength < 0) ? Formatter.UNFORMATTED : 
      new LineFormatter(minLength,maxLength,flatFileOptions.getAlignment(),flatFileOptions.getPadCharacter());

    FlatRecordField flatRecordField;
    if (flatFileOptions.getSubfieldDelimiters().length > 0) {
      flatRecordField = new MultivaluedDelimitedField(name,label,offset,maxFieldWidth,fieldFormatter,
        defaultValueEvaluator, flatFileOptions);
    } else {
      flatRecordField = new DelimitedField(name,label,offset,maxFieldWidth,fieldFormatter,
        defaultValueEvaluator, flatFileOptions);
    }
    flatRecordFieldList.add(flatRecordField);
  }

  public boolean isFieldDelimited() {
    return true;
  }

  public boolean isBinary() {
    return false;
  }

  public boolean isText() {
    return true;
  }
  
}

