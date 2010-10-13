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

import com.servingxml.util.ServingXmlException;
import com.servingxml.util.Name;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.QualifiedName;
import com.servingxml.components.common.NameSubstitutionExpr;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactory;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactoryAssembler;
import com.servingxml.expr.substitution.IntegerSubstitutionExpr;

//  Revisit

/**
 * The <code>FlatRecordTypeFactoryAssembler</code> implements an assembler for
 * assembling flat file record type objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FlatRecordTypeFactoryAssembler extends FlatFileOptionsFactoryAssembler {
  private static final Name DEFAULT_RECORD_TYPE_NAME = new QualifiedName("record");
                                                       
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private FlatRecordFieldFactory[] fieldTypeFactories = new FlatRecordFieldFactory[0];
  private String recordLength = null;

  private String recordTypeName = "record";   

  public void setName(String recordTypeName) {
    this.recordTypeName = recordTypeName;
  }

  public void setRecordLength(String recordLength) {
    this.recordLength = recordLength;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(FlatRecordFieldFactory[] fieldTypeFactories) {

    this.fieldTypeFactories = fieldTypeFactories;
  }

  public FlatRecordTypeFactory assemble(ConfigurationContext context) {
    if (recordTypeName.length() == 0) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
                                                                 context.getElement().getTagName(),
                                                                 "name");
      throw new ServingXmlException(message);
    }

    IntegerSubstitutionExpr recordLengthExpr = recordLength == null? IntegerSubstitutionExpr.NULL : 
      IntegerSubstitutionExpr.parseInt(context.getQnameContext(), recordLength);

    NameSubstitutionExpr recordTypeNameExpr = NameSubstitutionExpr.parse(context.getQnameContext(),recordTypeName);
    
    FlatFileOptionsFactory flatFileOptionsFactory = assembleFlatFileOptions(context);

    FlatRecordTypeFactory recordTypeFactory = new FlatRecordTypeFactoryImpl(recordTypeNameExpr, fieldTypeFactories, 
      recordLengthExpr, flatFileOptionsFactory);
    if (parameterDescriptors.length > 0) {
      recordTypeFactory = new FlatRecordTypeFactoryPrefilter(recordTypeFactory,parameterDescriptors);
    }
    return recordTypeFactory;
  }
}
