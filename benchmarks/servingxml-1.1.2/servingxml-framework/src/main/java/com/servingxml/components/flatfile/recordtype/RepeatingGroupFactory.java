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

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactory;
import com.servingxml.components.flatfile.options.RecordDelimiterFactory;
import com.servingxml.expr.substitution.IntegerSubstitutionExpr;
import com.servingxml.util.Name;

/**
 * The <code>RepeatingGroupFactory</code> is a <code>FlatRecordFieldFactory</code> factory for creating repeating group fields.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class RepeatingGroupFactory implements FlatRecordFieldFactory {
  private final Name fieldName;
  private final FlatRecordTypeFactory flatRecordTypeFactory;
  private final IntegerSubstitutionExpr countExpr;
  private final IntegerSubstitutionExpr startExpr;
  private final FlatFileOptionsFactory flatFileOptionsFactory;               

  public RepeatingGroupFactory(Name fieldName, IntegerSubstitutionExpr startExpr, 
                               FlatRecordTypeFactory flatRecordTypeFactory, 
    IntegerSubstitutionExpr countExpr, FlatFileOptionsFactory flatFileOptionsFactory) {
    this.fieldName = fieldName;
    this.startExpr = startExpr;
    this.flatRecordTypeFactory = flatRecordTypeFactory;
    this.countExpr = countExpr;
    this.flatFileOptionsFactory = flatFileOptionsFactory;
  }                                       
                                        
  public void appendFlatRecordField(ServiceContext context, Flow flow,
    FlatFileOptions defaultOptions, List<FlatRecordField> flatRecordFieldList) {
    FlatFileOptions flatFileOptions = flatFileOptionsFactory.createFlatFileOptions(context, flow, defaultOptions);

    FlatRecordType flatRecordType = flatRecordTypeFactory.createFlatRecordType(context, flow, flatFileOptions);
    if (flatFileOptions.getRepeatDelimiters().length > 0) {
      //System.out.println("There are repeat delimiters!!!");
      FlatRecordField repeatingGroup = new DelimitedRepeatingGroup(fieldName, startExpr, flatRecordType,flatFileOptions,
        countExpr);
      flatRecordFieldList.add(repeatingGroup);
    } else {
      //System.out.println("Creating fixed repeating group");
      FlatRecordField repeatingGroup = new FixedRepeatingGroup(fieldName, startExpr, flatRecordType, countExpr, flatFileOptions);
      flatRecordFieldList.add(repeatingGroup);
    }
  }

  public boolean isFieldDelimited() {
    return flatFileOptionsFactory.hasRepeatDelimiters() ? true : flatRecordTypeFactory.isFieldDelimited();
  }

  public boolean isBinary() {
    return flatRecordTypeFactory.isBinary();
  }

  public boolean isText() {
    return flatRecordTypeFactory.isText();
  }
}
