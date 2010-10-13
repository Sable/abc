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

/**
 * The <code>LiteralSubstitutionExpr</code> implements a class that
 * evaluates to a literal value.
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class LiteralSubstitutionExpr extends SubstitutionExpr {
  private final String value;
  
  public LiteralSubstitutionExpr(String value) {
    this.value = value;
  }
  
  public String evaluateAsString(Record parameters, Record record) {
    return value;
  }
  
  public String[] evaluateAsStringArray(Record parameters, Record record) {
    return new String[]{value};
  }
  
  public boolean isLiteral() {
    return true;
  }

  public boolean isNull() {
    return false;
  }
}
