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
import com.servingxml.components.common.SimpleNameEvaluator;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactory;
import com.servingxml.expr.substitution.IntegerSubstitutionExpr;
import com.servingxml.util.Name;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.SystemConstants;
import java.util.List;
import com.servingxml.components.common.NameSubstitutionExpr;

/**
 * Class for flat file record type objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class VbsFlatRecordTypeFactory implements FlatRecordTypeFactory {

  private final FlatRecordFieldFactory[] sdwFieldTypeFactories;
  private final RecordCombinationFactory[] recordCombinationFactories;
  private final FlatFileOptionsFactory flatFileOptionsFactory;

  public VbsFlatRecordTypeFactory(FlatRecordFieldFactory[] sdwFieldTypeFactories,
                                        RecordCombinationFactory[] recordCombinationFactories,
                                        FlatFileOptionsFactory flatFileOptionsFactory) {

    this.sdwFieldTypeFactories = sdwFieldTypeFactories;
    this.recordCombinationFactories = recordCombinationFactories;
    this.flatFileOptionsFactory = flatFileOptionsFactory;
  }

  public FlatRecordType createFlatRecordType(ServiceContext context, Flow flow, 
                                             FlatFileOptions defaultOptions) {
    FlatFileOptions flatFileOptions = flatFileOptionsFactory.createFlatFileOptions(context, flow, defaultOptions);

    Name headerRecordTypeName = new QualifiedName(SystemConstants.SERVINGXML_NS_URI,"tag");
    NameSubstitutionExpr headerRecordTypeNameResolver = new SimpleNameEvaluator(headerRecordTypeName);
    FlatRecordTypeFactory sdwRecordTypeFactory = new FlatRecordTypeFactoryImpl(headerRecordTypeNameResolver, 
                                                                               sdwFieldTypeFactories, 
                                                                               IntegerSubstitutionExpr.NULL, 
                                                                               flatFileOptionsFactory); 
    FlatRecordType sdwRecordType = sdwRecordTypeFactory.createFlatRecordType(context, 
                                                                             flow, 
                                                                             defaultOptions);
    FlatRecordType[] flatRecordTypes = new FlatRecordType[recordCombinationFactories.length];
    for (int i = 0; i < recordCombinationFactories.length; ++i) {
      flatRecordTypes[i] = recordCombinationFactories[i].createFlatRecordType(context,flow,sdwRecordType,defaultOptions);
    }

    FlatRecordType defaultFlatRecordType = new VbsFlatRecordType(flatRecordTypes);
    return defaultFlatRecordType;
  }

  public void appendFlatRecordField(ServiceContext context, Flow flow,
                                    FlatFileOptions defaultOptions, List<FlatRecordField> flatRecordFieldList) {
  }

  public boolean isFieldDelimited() {
    boolean result = true;
    for (int i = 0; result && i < recordCombinationFactories.length; ++i) {
      if (!recordCombinationFactories[i].isFieldDelimited()) {
        result = false;
      }
    }

    return result;
  }

  public boolean isBinary() {
    boolean result = true;
    for (int i = 0; result && i < recordCombinationFactories.length; ++i) {
      if (!recordCombinationFactories[i].isBinary()) {
        result = false;
      }
    }

    return result;
  }

  public boolean isText() {
    boolean result = true;
    for (int i = 0; result && i < recordCombinationFactories.length; ++i) {
      if (!recordCombinationFactories[i].isText()) {
        result = false;
      }
    }

    return result;
  }
}
