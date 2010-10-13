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

package com.servingxml.expr.substitution;

import com.servingxml.util.ServingXmlException;
import com.servingxml.util.Name;
import com.servingxml.util.record.Record;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;

/**
 * The <code>ParameterSubstitutor</code> implements a class that
 * does parameter substitution in strings.
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ParameterSubstitutor extends SubstitutionExpr {
  private final Name parameterName;
  
  public ParameterSubstitutor(Name parameterName) {
    this.parameterName = parameterName;
  }
  
  public String evaluateAsString(Record parameters,Record record) {
    String value = parameters.getString(parameterName);
    if (value == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.PARAMETER_NOT_FOUND, 
                                                                 parameterName.toString());
      throw new ServingXmlException(message);
    }
    return value;
  }
  
  public String[] evaluateAsStringArray(Record parameters,Record record) {
    String[] values = parameters.getStringArray(parameterName);
    if (values == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.PARAMETER_NOT_FOUND, 
                                                                 parameterName.toString());
    }
    return values;
  }
  
  public boolean isLiteral() {
    return false;
  }

  public boolean isNull() {
    return false;
  }
}
