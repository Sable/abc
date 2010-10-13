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

import java.util.ArrayList;
import java.util.StringTokenizer;

import com.servingxml.util.Name;
import com.servingxml.ioc.components.ConfigurationContext;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ReorderRecordsAssembler {
  
  private String recordTypeQnames = "";
  

  public void setRecordTypes(String recordTypeQnames) {
    this.recordTypeQnames = recordTypeQnames;
  }
                                                       
  public Sort assemble(ConfigurationContext context) {

    //System.out.println("ReorderRecordsAssembler.assemble start " + recordTypeQnames);

    StringTokenizer recordTypeTokenizer = new StringTokenizer(recordTypeQnames," ,");
    ArrayList<Name> recordTypeList = new ArrayList<Name>();
    while (recordTypeTokenizer.hasMoreTokens()) {
      String recordTypeQname = recordTypeTokenizer.nextToken();
      Name fieldName = context.getQnameContext().createName(recordTypeQname);
      recordTypeList.add(fieldName);
    }
    Name[] recordTypeNames = new Name[recordTypeList.size()];
    recordTypeNames = recordTypeList.toArray(recordTypeNames);

    //System.out.println("ReorderRecordsAssembler.assemble end");
    return new ReorderRecords(recordTypeNames);
  }
}

