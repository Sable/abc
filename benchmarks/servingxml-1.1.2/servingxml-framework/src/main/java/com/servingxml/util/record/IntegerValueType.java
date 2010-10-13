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

package com.servingxml.util.record;

import java.sql.Types;

import com.servingxml.util.ServingXmlException;

public class IntegerValueType implements ValueType {

  public Object[] fromStringArray(String[] sa) {
    String s = "";
    try {
      Object[] a = new Object[sa.length];
      for (int i = 0; i < a.length; ++i) {
        s = sa[i];
        a[i] = Integer.valueOf(s);
      }
      return a;
    } catch (NumberFormatException e) {
      String message = "Invalid integer value \"" + s + "\"";
      throw new ServingXmlException(message, e);
    }
  }

  public Object fromString(String s) {
    try {
      Object o = Integer.valueOf(s);
      return o;
    } catch (NumberFormatException e) {
      String message = "Invalid integer value \"" + s + "\"";
      throw new ServingXmlException(message, e);
    }
  }

  public String toString(Object o) {
    return o == null ? "" : o.toString();
  }

  public Object getSqlValue(Object o) {
    return o;
  }

  public int getSqlType() {
    return Types.INTEGER;
  }
}
