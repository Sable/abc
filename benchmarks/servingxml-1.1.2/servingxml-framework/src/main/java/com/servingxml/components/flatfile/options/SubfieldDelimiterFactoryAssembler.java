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

package com.servingxml.components.flatfile.options;

import com.servingxml.util.ServingXmlException;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.StringHelper;
import com.servingxml.expr.substitution.SubstitutionExpr;

/**
 * The <code>SubfieldDelimiterFactoryAssembler</code> implements an assembler for
 * assembling <code>SubfieldDelimiter</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SubfieldDelimiterFactoryAssembler {

  private String value = null;
  private SeparatorFactory separatorFactory = null;
  private String escapedBy = "";

  public void setEscapeCharacter(String escapedBy) {
    this.escapedBy = escapedBy;
  }
  
  public void setValue(String value) {
    this.value = value;
  }

  public void injectComponent(SeparatorFactory separatorFactory) {
    this.separatorFactory = separatorFactory;
  }
                                                       
  public SubfieldDelimiterFactory assemble(ConfigurationContext context) {

    if (separatorFactory == null) {
      if (value == null) {
        String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,context.getElement().getTagName(),"value");
        throw new ServingXmlException(message);
      }
      value = StringHelper.translateEscapeChars(value);
      SubstitutionExpr valueExpr = SubstitutionExpr.parseString(context.getQnameContext(),value);
      SubstitutionExpr escapedByExpr = SubstitutionExpr.parseString(context.getQnameContext(),escapedBy);
      separatorFactory = new DefaultSeparatorFactory(valueExpr, escapedByExpr);
    }
    
    SubfieldDelimiterFactory delimiterFactory = new SubfieldDelimiterFactory(separatorFactory);
    
    return delimiterFactory;
  }
}
