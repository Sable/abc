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
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.expr.substitution.IntegerSubstitutionExpr;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactory;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactoryAssembler;

public class RepeatingFieldFactoryAssembler extends FlatFileOptionsFactoryAssembler {

  private FlatRecordFieldFactory fieldFactory = null;
  private String count = "";

  public void setCount(String count) {
    this.count = count;
  }

  public void injectComponent(FlatRecordFieldFactory fieldFactory) {
    this.fieldFactory = fieldFactory;
  }

  public FlatRecordFieldFactory assemble(ConfigurationContext context) {

    if (fieldFactory == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,
        context.getElement().getTagName(),"sx:flatRecordField");
      throw new ServingXmlException(message);
    }

    IntegerSubstitutionExpr countExpr = (count.length() == 0) ? IntegerSubstitutionExpr.MAX_INTEGER :
      IntegerSubstitutionExpr.parseInt(context.getQnameContext(),count);

    FlatFileOptionsFactory flatFileOptionsFactory = assembleFlatFileOptions(context);
    FlatRecordFieldFactory repeatingFieldFactory = new RepeatingFieldFactory(fieldFactory, 
      countExpr, flatFileOptionsFactory);

    return repeatingFieldFactory;
  }
}

