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

public class ArrayValueType extends ValueType {

  private final Class type;
  private final ValueType componentType;

  public ArrayValueType(Class type) {
    this.type = type;
    this.componentType = createValueType(type.getComponentType());
  }
  
  public boolean isArray() {
    return true;
  }

  public Class getType() {
    return type;
  }

  public Object valueOfString(String stringValue) {
    return valueOf(new String[]{stringValue});
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
      Object b = Array.newInstance(componentType.getType(),length);
      for (int i = 0; i < length; ++i) {
        Object c = Array.get(o,i);
        Object c2 = componentType.valueOf(c);
        Array.set(b,i,c2);
      }
      value = b;                          
    } else {
      Object b = Array.newInstance(componentType.getType(),1);
      if (componentType.getType().isAssignableFrom(oClass)) {
        Array.set(b,0,o);
      } else {
        Array.set(b,0,componentType.valueOf(o));
      }
      value = b;
    }

    return value;
  }
}
