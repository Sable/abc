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

import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.components.regex.PatternMatcherFactory;
import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.components.common.Validator;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public class ValidateRecordAssembler {
  private PatternMatcherFactory patternMatcherFactory = PatternMatcherFactory.DEFAULT;
  
  private Name recordTypeName = Name.EMPTY;
  private String message = "";
  private Validator[] fieldValidations = new Validator[0];
  
  public void setRecordType(Name recordTypeName) {
    this.recordTypeName = recordTypeName;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void injectComponent(Validator[] fieldValidations) {
    this.fieldValidations = fieldValidations;
  }

  public Validator assemble(ConfigurationContext context) {
    
    SubstitutionExpr messageExpr = SubstitutionExpr.parseString(context.getQnameContext(),message);

    ValidateRecord validation = new ValidateRecord(recordTypeName, fieldValidations, messageExpr);
    
    return validation;
  }
}

