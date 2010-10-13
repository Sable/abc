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
 * The <code>RepeatDelimiterFactoryAssembler</code> implements an assembler for
 * assembling <code>RepeatDelimiter</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class RepeatDelimiterFactoryAssembler {
  
  private String start = "";
  private String end = "";
  private String value = null;
  private SeparatorFactory separatorFactory = null;
  private String escapedBy = "";

  public void setEscapeCharacter(String escapedBy) {
    this.escapedBy = escapedBy;
  }

  public void setStart(String start) {
    this.start = start;
  }

  public void setEnd(String end) {
    this.end = end;
  }
  
  public void setValue(String value) {
    this.value = value;
  }

  public void injectComponent(SeparatorFactory separatorFactory) {
    this.separatorFactory = separatorFactory;
  }
                                                       
  public RepeatDelimiterFactory assemble(ConfigurationContext context) {

    if (separatorFactory == null) {
      if (start.length() > 0) {
        if (end.length() == 0) {
          String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,context.getElement().getTagName(),"end");
          throw new ServingXmlException(message);
        }
        start = StringHelper.translateEscapeChars(start);
        end = StringHelper.translateEscapeChars(end);
        SubstitutionExpr startExpr = SubstitutionExpr.parseString(context.getQnameContext(),start);
        SubstitutionExpr endExpr = SubstitutionExpr.parseString(context.getQnameContext(),end);
        separatorFactory = new StartEndSeparatorFactory(startExpr, endExpr);
      } else {
        if (value == null) {
          String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,context.getElement().getTagName(),"value");
          throw new ServingXmlException(message);
        }
        value = StringHelper.translateEscapeChars(value);
        SubstitutionExpr valueExpr = SubstitutionExpr.parseString(context.getQnameContext(),value);
        SubstitutionExpr escapedByExpr = SubstitutionExpr.parseString(context.getQnameContext(),escapedBy);
        separatorFactory = new DefaultSeparatorFactory(valueExpr, escapedByExpr);
      }
    }
    
    RepeatDelimiterFactory delimiterFactory = new RepeatDelimiterFactory(separatorFactory);
    
    return delimiterFactory;
  }
}
