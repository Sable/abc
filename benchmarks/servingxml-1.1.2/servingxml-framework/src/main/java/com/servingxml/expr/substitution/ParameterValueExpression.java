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
import com.servingxml.util.record.Value;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;

/**
 * The <code>ParameterValueExpression</code> implements a class that
 * does parameter substitution in strings.
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ParameterValueExpression implements ValueExpression {
  private final Name parameterName;
  
  public ParameterValueExpression(Name parameterName) {
    this.parameterName = parameterName;
  }
  
  public Value evaluateValue(Record parameters,Record record) {
    Value value = parameters.getValue(parameterName);
    if (value == null) {
      value = Value.EMPTY;
      //String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.PARAMETER_NOT_FOUND, 
      //                                                           parameterName.toString());
      //throw new ServingXmlException(message);
    }
    return value;
  }
}
