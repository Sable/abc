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

package com.servingxml.components.recordmapping;

import com.servingxml.util.Name;
import com.servingxml.util.record.Record;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */


class OrderBy implements Sort {
  private final Name fieldName;

  public OrderBy(Name fieldName) {
    this.fieldName = fieldName;
  }

  public int compare(Record record1, Record record2) {
    String value1 = record1.getString(fieldName);
    String value2 = record2.getString(fieldName);

    int diff = 0;
    if (value1 != null && value2 == null) {
      diff = 1;
    } else if (value1 == null && value2 != null) {
      diff = -1;
    } else if (value1 == null && value2 == null) {
      diff = 0;
    } else {
      diff = value1.compareTo(value2);
    }

    return diff;
  }
}

