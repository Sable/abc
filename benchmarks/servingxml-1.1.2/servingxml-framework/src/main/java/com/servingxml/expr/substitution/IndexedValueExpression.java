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

import com.servingxml.util.record.Record;
import com.servingxml.util.record.Value;
import com.servingxml.util.record.ScalarValue;
import com.servingxml.util.record.ValueTypeFactory;

/**
 * The <code>IndexedValueExpression</code> implements a 
 * <code>ValueExpression</code>. 
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */
                             
public class IndexedValueExpression implements ValueExpression {
  private final ValueExpression expr;
  private final int index;

  public IndexedValueExpression(ValueExpression expr, int index) {
    this.expr = expr;
    this.index = index;
  }

  public Value evaluateValue(Record parameters, Record record) {
    Value result = Value.EMPTY;
    Value value = expr.evaluateValue(parameters,record);
    if (value != null) {
      String[] a = value.getStringArray();
      if (index < a.length) {
        value = new ScalarValue(a[index], ValueTypeFactory.STRING_TYPE);
      }
    }
    return result;
  }
}
