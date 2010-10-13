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
import java.util.ArrayList;

import com.servingxml.expr.substitution.IntegerSubstitutionExpr;
import com.servingxml.util.ServingXmlException;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactory;

public class RepeatingFieldFactory implements FlatRecordFieldFactory {

  private final FlatRecordFieldFactory fieldFactory;
  private final IntegerSubstitutionExpr countExpr;
  private final FlatFileOptionsFactory flatFileOptionsFactory;

  public RepeatingFieldFactory(FlatRecordFieldFactory fieldFactory, 
    IntegerSubstitutionExpr countExpr, FlatFileOptionsFactory flatFileOptionsFactory) {

    this.fieldFactory = fieldFactory;
    this.countExpr = countExpr;
    this.flatFileOptionsFactory = flatFileOptionsFactory;
  }

  public void appendFlatRecordField(ServiceContext context, Flow flow,
    FlatFileOptions defaultOptions, List<FlatRecordField> flatRecordFieldList) {
    FlatFileOptions flatFileOptions = flatFileOptionsFactory.createFlatFileOptions(context, flow, defaultOptions);

    List<FlatRecordField> fieldList = new ArrayList<FlatRecordField>();
    fieldFactory.appendFlatRecordField(context, flow, flatFileOptions, fieldList);
    if (fieldList.size() != 1) {
      throw new ServingXmlException("Expected one field");
    }
    FlatRecordField field = fieldList.get(0);
    FlatRecordField repeatingField = new RepeatingField(field, countExpr, flatFileOptions);
    flatRecordFieldList.add(repeatingField);
  }

  public boolean isFieldDelimited() {
    return true;
  }

  public boolean isBinary() {
    return fieldFactory.isBinary();
  }

  public boolean isText() {
    return fieldFactory.isText();
  }
  
}

