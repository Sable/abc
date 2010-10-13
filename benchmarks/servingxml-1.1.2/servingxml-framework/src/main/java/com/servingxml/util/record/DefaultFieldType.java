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

import com.servingxml.util.Name;

public class DefaultFieldType implements FieldType {
  private final Name fieldName;
  private final String label;

  public DefaultFieldType(Name fieldName) {
    this.fieldName = fieldName;
    this.label = fieldName.getLocalName();
  }

  public DefaultFieldType(Name fieldName, String label) {
    this.fieldName = fieldName;
    this.label = label;
  }

  public Name getName() {
    return fieldName;
  }

  public String getLabel() {
    return label;
  }
}
