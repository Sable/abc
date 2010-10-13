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
import com.servingxml.util.SystemConstants;

/**
 * The <code>SubstitutionExpr</code> implements a class that
 * does parameter substitution in strings.
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */
                             
public class IndexedSubstitutionExpression extends SubstitutionExpr {
  private final SubstitutionExpr expr;
  private final int index;

  public IndexedSubstitutionExpression(SubstitutionExpr expr, int index) {
    this.expr = expr;
    this.index = index;
  }

  public String evaluateAsString(Record parameters, Record record) {
    String[] values = expr.evaluateAsStringArray(parameters,record);
    return index < values.length ? values[index] : "";
  }

  public String[] evaluateAsStringArray(Record parameters, Record record) {
    String[] values = expr.evaluateAsStringArray(parameters,record);
    return index < values.length ? new String[]{values[index]} : SystemConstants.EMPTY_STRING_ARRAY;
  }

  public boolean isLiteral() {
    return expr.isLiteral();
  }

  public boolean isNull() {
    return false;
  }
}
