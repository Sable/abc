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

package com.servingxml.components.recordio;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;

/**
 *
 * 
 * @author  Daniel A. Parker
 */


class RecordSorter extends AbstractRecordFilter {       
  private final Comparator comparator;
  private List<Flow> recordList = new ArrayList<Flow>();

  public RecordSorter(Comparator comparator) {
    this.comparator = comparator;
  }

  public void writeRecord(ServiceContext context, Flow flow) {
    recordList.add(flow);
  }

  public void startRecordStream(ServiceContext context, Flow flow) {
    super.startRecordStream(context, flow);
  }

  public void endRecordStream(ServiceContext context, Flow flow) {
    if (recordList.size() > 0) {
      Flow[] entries = new Flow[recordList.size()];
      entries = recordList.toArray(entries);
      Arrays.sort(entries,comparator);
      for (int i = 0; i < entries.length; ++i) {
        getRecordWriter().writeRecord(context, entries[i]);
      }
    }

    super.endRecordStream(context, flow);
  }

  public void close() {
    super.close();
  }
}

