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

package com.servingxml.components.recordio;

import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.components.regex.PatternMatcher;
import com.servingxml.components.regex.PatternMatcherFactory;
import com.servingxml.components.common.TrueFalseEnum;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public class FieldRestrictionAssembler {
  private PatternMatcherFactory patternMatcherFactory = PatternMatcherFactory.DEFAULT;
  
  private Name fieldName = null;
  private String pattern = null;
  private String negate = TrueFalseEnum.FALSE.toString();
  
  public void setField(Name fieldName) {
    this.fieldName = fieldName;
  }

  //  Deprecated
  public void setMatch(String pattern) {
    this.pattern = pattern;
  }

  public void setPattern(String pattern) {
    this.pattern = pattern;
  }

  public void setNegate(String negate) {
    this.negate = negate;
  }

  public void injectComponent(PatternMatcherFactory patternMatcherFactory) {
    this.patternMatcherFactory = patternMatcherFactory;
  }

  public Restriction assemble(ConfigurationContext context) {
    
    if (fieldName == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
        context.getElement().getTagName(),"field");
      throw new ServingXmlException(message);
    }

    TrueFalseEnum negateIndicator;
    try {
      negateIndicator = TrueFalseEnum.parse(negate);
    } catch (ServingXmlException e) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_VALUE_INVALID,
        context.getElement().getTagName(), "negate");
      e = e.supplementMessage(message);
      throw e;
    }

    if (pattern == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
        context.getElement().getTagName(),"pattern");
      throw new ServingXmlException(message);
    }
    PatternMatcher matcher = patternMatcherFactory.createPatternMatcher(pattern);

    Restriction criteria = new FieldRestriction(fieldName,matcher);
    if (negateIndicator.booleanValue()) {
      criteria = new NegateRestriction(criteria);
    }
    
    return criteria;
  }
}

