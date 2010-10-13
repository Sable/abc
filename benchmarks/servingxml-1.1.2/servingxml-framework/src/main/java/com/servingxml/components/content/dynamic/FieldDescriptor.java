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

import java.text.SimpleDateFormat;
import java.text.ParsePosition;

import com.servingxml.util.Name;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.Value;

public class FieldDescriptor {
  private final String accessorName;
  private final String propertyName;
  private final Class propertyType;
  private final int index;
  private final ValueType valueType;

  public FieldDescriptor(String accessorName, int index, 
  String propertyName, Class propertyType) {
    this.accessorName = accessorName;
    this.index = index;
    this.propertyType = propertyType;
    this.propertyName = propertyName;
    this.valueType = ValueType.createValueType(propertyType);
  }

  public Class getPropertyType() {
    return propertyType;
  }

  public String getAccessorName() {
    return accessorName;
  }

  public int getIndex() {
    return index;
  }

  public String getPropertyName() {
    return propertyName;
  }                        

  public Object getValue(Name fieldName, Record record) {
    Object value = null;
    Value fieldValue = record.getValue(fieldName);
    if (fieldValue != null) {
      value = valueType.valueOf(fieldValue.getStringArray());
    }

    return value;
  }

  public ValueType getValueType() {
    return valueType;
  }
}

