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
 * The <code>MultiPartSubstitutionExpr</code> implements a class that
 * when evaluated concatenates the valueResolvers of its parts.
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class MultiPartSubstitutionExpr extends SubstitutionExpr {
  private final SubstitutionExpr[] valueResolvers;
  
  public MultiPartSubstitutionExpr(SubstitutionExpr[] valueResolvers) {
    this.valueResolvers = valueResolvers;
  }
  
  public String evaluateAsString(Record parameters, Record record) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < valueResolvers.length; ++i) {
      SubstitutionExpr subExpr = valueResolvers[i];
      String value = subExpr.evaluateAsString(parameters, record);
      if (value != null && value.length() > 0) {
        builder.append(value);
      }
    }
    return builder.toString();
  }
  
  public String[] evaluateAsStringArray(Record parameters, Record record) {
    
    String[] values = SystemConstants.EMPTY_STRING_ARRAY;
    for (int i = 0; i < valueResolvers.length; ++i) {
      SubstitutionExpr resolver = valueResolvers[i];
      
      String[] partValues = resolver.evaluateAsStringArray(parameters, record);
      if (values.length == 0) {
        values = new String[partValues.length];
        for (int k = 0; k < partValues.length; ++k) {
          values[k] = partValues[k];
        }
      } else if (partValues.length >= 1) {
        String[] prevValues = values;
        values = new String[partValues.length*prevValues.length];
        for (int j = 0; j < prevValues.length; ++j) {
          for (int k = 0; k < partValues.length; ++k) {
            values[j+k] = prevValues[j] + partValues[k];
          }
        }
      }
    }
    return values;
  }
  
  public boolean isLiteral() {
    boolean resolved = true;
    for (int i = 0; resolved && i < valueResolvers.length; ++i) {
      SubstitutionExpr subExpr = valueResolvers[i];
      if (!subExpr.isLiteral()) {
        resolved = false;
      }
    }
    return resolved;
  }

  public boolean isNull() {
    return false;
  }
}
