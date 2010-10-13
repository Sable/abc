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
import com.servingxml.util.SystemConstants;

/**
 * The <code>SubstitutionExpr</code> implements a class that
 * does parameter substitution in strings.
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FieldSubstitutor extends SubstitutionExpr {
  private final Name fieldName;
  
  public FieldSubstitutor(Name fieldName) {
    this.fieldName = fieldName;
  }
  
  public String evaluateAsString(Record parameters,Record record) {

   //System.out.println(getClass().getName()+".evaluateAsString " + fieldName + " start");
    String value = record.getString(fieldName);
    if (value == null) {
      value = "";
     //System.out.println(getClass().getName()+".evaluateAsString null value");
      //throw new ServingXmlException(message);
    } else {
       //System.out.println(getClass().getName()+".evaluateAsString " + fieldName + ", value=" + value + " end");
    }
    return value;
  }
  
  public String[] evaluateAsStringArray(Record parameters,Record record) {
   //System.out.println(getClass().getName()+".evaluateAsStringArray " + fieldName + " start");
    String[] values = record.getStringArray(fieldName);
    if (values == null) {
     //System.out.println(getClass().getName()+".evaluateAsString null value");
      //throw new ServingXmlException(message);
      values = SystemConstants.EMPTY_STRING_ARRAY;
    }
    for (int i = 0;  i < values.length; ++i) {
     //System.out.println(values[i]);
    }
   //System.out.println(getClass().getName()+".evaluateAsStringArray " + fieldName + " end");
    return values;
  }
  
  public boolean isLiteral() {
    return false;
  }

  public boolean isNull() {
    return false;
  }
}
