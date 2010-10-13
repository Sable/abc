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

package com.servingxml.components.content.dynamic;

import java.lang.reflect.Array;

/**
 *
 *  
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ScalarValueType extends ValueType {


  private final Class type;

  public ScalarValueType(Class type) {
    this.type = type;
  }

  public Class getType() {
    return type;
  }
  
  public boolean isArray() {
    return true;
  }
  
  public Object valueOfString(String stringValue) {
    Object value = null;
    if (stringValue != null) {
      if (type.isPrimitive()) {
        if (type == Boolean.TYPE) {
          value = new Boolean(stringValue);
        } else if (type == Character.TYPE && stringValue.length() > 0) {
          value = new Character(stringValue.charAt(0));
        } else if (type == Byte.TYPE && stringValue.length() > 0) {
          value = new Byte(stringValue);
        } else if (type == Short.TYPE) {
          value = new Short(stringValue);
        } else if (type == Integer.TYPE) {
          value = new Integer(stringValue);
        } else if (type == Long.TYPE) {
          value = new Long(stringValue);
        } else if (type == Float.TYPE) {
          value = new Float(stringValue);
        } else if (type == Double.TYPE) {
          value = new Double(stringValue);
        }
      } else if (type.isAssignableFrom(String.class)) {
          value = stringValue;
      }
    }
    return value;
  }

  public boolean isAssignableFrom(ValueType rhs) {
    boolean areEqual = false;
    if (type.isAssignableFrom(rhs.getType())) {
      areEqual = true;
    }
    return areEqual;
  }

  public Object valueOf(Object o) {
    Object value = null;
    Class oClass = o.getClass();
    if (type.isAssignableFrom(oClass)) {
      value = o;
    } else if (oClass.isArray()) {
        int length = Array.getLength(o);
        for (int i = 0; i < length && i < 1; ++i) {
          Object x = Array.get(o,i);
          value = x;
        }
    } else {
      String s = o.toString();
      value = valueOfString(s);
    }

    return value;
  }
}
