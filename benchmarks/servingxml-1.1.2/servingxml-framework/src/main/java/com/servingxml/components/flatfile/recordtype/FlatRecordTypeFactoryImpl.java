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

import com.servingxml.components.common.NameSubstitutionExpr;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactory;
import com.servingxml.expr.substitution.IntegerSubstitutionExpr;

/**
 * Class for flat file record type objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FlatRecordTypeFactoryImpl implements FlatRecordTypeFactory {

  private final FlatRecordFieldFactory[] fieldTypeFactories;
  private final NameSubstitutionExpr recordTypeNameExpr;
  private final IntegerSubstitutionExpr recordLengthExpr;
  private final FlatFileOptionsFactory flatFileOptionsFactory;

  public FlatRecordTypeFactoryImpl(NameSubstitutionExpr recordTypeNameExpr, 
                                   FlatRecordFieldFactory[] fieldTypeFactories, 
                                   IntegerSubstitutionExpr recordLengthExpr,
                                   FlatFileOptionsFactory flatFileOptionsFactory) {

    this.fieldTypeFactories = fieldTypeFactories;
    this.recordTypeNameExpr = recordTypeNameExpr;
    this.recordLengthExpr = recordLengthExpr;
    this.flatFileOptionsFactory = flatFileOptionsFactory;
  }

  public FlatRecordType createFlatRecordType(ServiceContext context, Flow flow, FlatFileOptions defaultOptions) {
    FlatFileOptions flatFileOptions = flatFileOptionsFactory.createFlatFileOptions(context, flow, defaultOptions);

    List<FlatRecordField> flatRecordFieldList = new ArrayList<FlatRecordField>();
    for (int i = 0; i < fieldTypeFactories.length; ++i) {
      fieldTypeFactories[i].appendFlatRecordField(context, flow, flatFileOptions, flatRecordFieldList);
    }
    FlatRecordField[] flatRecordFields = new FlatRecordField[flatRecordFieldList.size()];
    flatRecordFields = (FlatRecordField[])flatRecordFieldList.toArray(flatRecordFields);

    FlatRecordType flatRecordType = new FlatRecordTypeImpl(recordTypeNameExpr, flatRecordFields, 
                                                           recordLengthExpr, flatFileOptions);
    return flatRecordType;
  }

  public void appendFlatRecordField(ServiceContext context, Flow flow,
                                    FlatFileOptions defaultOptions, List<FlatRecordField> flatRecordFieldList) {
    FlatFileOptions flatFileOptions = flatFileOptionsFactory.createFlatFileOptions(context, flow, defaultOptions);
    for (int i = 0; i < fieldTypeFactories.length; ++i) {
      fieldTypeFactories[i].appendFlatRecordField(context, flow, flatFileOptions, flatRecordFieldList);
    }
  }

  public boolean isFieldDelimited() {
    boolean delimited = true;
    for (int i = 0; delimited && i < fieldTypeFactories.length; ++i) {
      if (!fieldTypeFactories[i].isFieldDelimited()) {
        delimited = false;
      }
    }
    return delimited;
  }

  public boolean isBinary() {
    boolean binary = true;
    for (int i = 0; binary && i < fieldTypeFactories.length; ++i) {
      if (!fieldTypeFactories[i].isBinary()) {
        binary = false;
      }
    }
    return binary;
  }

  public boolean isText() {
    boolean textValue = true;
    for (int i = 0; textValue && i < fieldTypeFactories.length; ++i) {
      if (!fieldTypeFactories[i].isText()) {
        textValue = false;
      }
    }
    return textValue;
  }
}
