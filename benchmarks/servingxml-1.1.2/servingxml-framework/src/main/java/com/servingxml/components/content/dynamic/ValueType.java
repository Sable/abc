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


public abstract class ValueType {
                                         
  public static final ValueType STRING_ARRAY_VALUE_TYPE = ValueType.createValueType(String[].class);

  public abstract Class getType();
  public abstract Object valueOfString(String stringValue);
  public abstract Object valueOf(Object o);
  public abstract boolean isAssignableFrom(ValueType rhs);
  public abstract boolean isArray();

  public static ValueType createValueType(Class type) {
    ValueType valueType;
    if (type.isArray()) {
      valueType = new ArrayValueType(type);
    } else {
      valueType = new ScalarValueType(type);
    }
    return valueType;
  }
}
