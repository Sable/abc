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

import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.expr.substitution.IntegerSubstitutionExpr;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactory;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactoryAssembler;
import com.servingxml.components.flatfile.options.RecordDelimiterFactory;

/**
 * Assembler for assembling a <code>RepeatingGroupFactory</code>.
 *
 *                      
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 * @see FlatRecordTypeChoiceFactory
 */

public class NonrepeatingGroupFactoryAssembler extends FlatFileOptionsFactoryAssembler {

  private FlatRecordTypeFactory flatRecordTypeFactory = null;
  private Name fieldName = null;
  private String start = "";
                                   
  public void setName(Name fieldName) {
    this.fieldName = fieldName;
  }                                                             

  public void setStart(String start) {
    this.start = start;
  }

  public void injectComponent(FlatRecordTypeFactory flatRecordTypeFactory) {
    this.flatRecordTypeFactory = flatRecordTypeFactory;
  }

  public FlatRecordFieldFactory assemble(final ConfigurationContext context) {

    if (fieldName == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
        context.getElement().getTagName(),"name");
      throw new ServingXmlException(message);
    }

    if (flatRecordTypeFactory == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,context.getElement().getTagName(),"sx:flatRecordType or sx:flatRecordTypeChoice");
      throw new ServingXmlException(message);
    }

    FlatFileOptionsFactory flatFileOptionsFactory = assembleFlatFileOptions(context);
    if (flatFileOptionsFactory.getSegmentDelimiterFactories() != null) {
      flatFileOptionsFactory.setRepeatDelimiterFactories(flatFileOptionsFactory.getSegmentDelimiterFactories());
    }
    flatFileOptionsFactory.setRecordDelimiterFactories(RecordDelimiterFactory.EMPTY_ARRAY);

    IntegerSubstitutionExpr startExpr = (start.length() == 0) ? IntegerSubstitutionExpr.ZERO :
      IntegerSubstitutionExpr.parseInt(context.getQnameContext(),start);
    FlatRecordFieldFactory repeatingGroupFactory = new RepeatingGroupFactory(fieldName, startExpr, 
      flatRecordTypeFactory, IntegerSubstitutionExpr.MAX_INTEGER, flatFileOptionsFactory);

    return repeatingGroupFactory;
  }
}


