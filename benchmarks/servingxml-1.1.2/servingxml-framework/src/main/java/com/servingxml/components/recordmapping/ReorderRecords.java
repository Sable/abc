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


class ReorderRecords implements Sort {
  private final Name[] recordTypeNames;
                                                                            
  public ReorderRecords(Name[] recordTypeNames) {
    this.recordTypeNames = recordTypeNames;
  }

  public int compare(Record record1, Record record2) {

    Name recordTypeName1 = record1.getRecordType().getName();
    Name recordTypeName2 = record2.getRecordType().getName();

    int diff = 0;

    for (int i = 0; diff == 0 && i < recordTypeNames.length; ++i) {
      Name recordTypeName = recordTypeNames[i];
      boolean b1 = recordTypeName1.equals(recordTypeName);
      boolean b2 = recordTypeName2.equals(recordTypeName);

      //System.out.println("" + recordTypeName + " " + recordTypeName1 + " " + recordTypeName2);

      if (b1 && !b2) {
        diff = -1;
      } else if (!b1 && b2) {
        diff = 1;
      }
    }
    //System.out.println("ReorderRecords.compare end " + diff);

    return diff;
  }
}

