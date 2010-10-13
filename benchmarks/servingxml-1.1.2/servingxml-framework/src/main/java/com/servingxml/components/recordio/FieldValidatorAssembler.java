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

import com.servingxml.components.regex.PatternMatcherFactory;
import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.components.common.Validator;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public class FieldValidatorAssembler {
  private PatternMatcherFactory patternMatcherFactory = PatternMatcherFactory.DEFAULT;
  
  private Name fieldName = null;
  private Restriction valueRestriction = Restriction.ALWAYS_ACCEPT;
  private Validator[] recordValidators = new Validator[0];
  private String message = null;
  
  public void setField(Name fieldName) {
    this.fieldName = fieldName;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void injectComponent(Validator[] recordValidators) {
    this.recordValidators = recordValidators;
  }

  public void injectComponent(Restriction valueRestriction) {
    this.valueRestriction = valueRestriction;
  }

  public FieldValidator assemble(ConfigurationContext context) {
    
    if (fieldName == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
        context.getElement().getTagName(),"field");
      throw new ServingXmlException(message);
    }

    if (message == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
        context.getElement().getTagName(),"message");
      throw new ServingXmlException(message);
    }

    SubstitutionExpr messageExpr = SubstitutionExpr.parseString(context.getQnameContext(),message);

    FieldValidator fieldValidation = new FieldValidator(fieldName, valueRestriction, recordValidators, messageExpr);
    
    return fieldValidation;
  }
}

