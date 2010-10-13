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

import com.servingxml.util.Name;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.Value;

/**
 * The <code>FieldValueExpression</code> implements a class that
 * does parameter substitution in strings.
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FieldValueExpression implements ValueExpression {
  private final Name fieldName;
  
  public FieldValueExpression(Name fieldName) {
    this.fieldName = fieldName;
  }
  
  public Value evaluateValue(Record parameters,Record record) {
    //System.out.println(getClass().getName()+".evaluateValue");
    Value value = record.getValue(fieldName);
    if (value == null) {
      value = Value.EMPTY;
    }
    return value;
  }
}
