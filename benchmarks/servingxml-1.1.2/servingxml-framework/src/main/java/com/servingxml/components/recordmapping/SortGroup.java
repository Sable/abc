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

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Arrays;

import org.xml.sax.helpers.AttributesImpl;

import com.servingxml.app.ServiceContext;
import com.servingxml.util.record.Record;
import com.servingxml.app.Flow;
import com.servingxml.util.xml.ExtendedContentHandler;

/**
 * Implements a factory class for creating <tt>OnGroupToXmlHandler</tt> instances.
 * 
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SortGroup implements MapXml {

  private final MapXml child;
  private final List<Flow> bufferedData;
  private final Comparator comparator;
  private Record firstRecord;
  private Record lastRecord;

  public SortGroup(MapXml child, Comparator comparator) {

    this.child = child;
    this.comparator = comparator;
    this.bufferedData = new ArrayList<Flow>();
  }

  public void writeRecord(ServiceContext context, Flow flow, 
    Record previousRecord, Record nextRecord, 
    ExtendedContentHandler handler, GroupState groupListener) {
    if (bufferedData.size() == 0) {
      this.firstRecord = previousRecord;
    }
    this.lastRecord = nextRecord;
    bufferedData.add(flow);

    //System.out.println("SortGroup.writeRecord recordCount = " + bufferedData.size());
  }

  public void groupStarted(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord, 
    ExtendedContentHandler handler, Record variables) {
    //System.out.println("SortGroup.groupStarted (before clear) recordCount = " + bufferedData.size());
    bufferedData.clear();
    child.groupStarted(context, flow,previousRecord,nextRecord,handler,variables);
  }


  public void groupStopped(ServiceContext context, Flow flow, ExtendedContentHandler handler) {
    writeRecords(context, handler, GroupState.DEFAULT);
    child.groupStopped(context, flow, handler);
  }

  private void writeRecords(ServiceContext context, ExtendedContentHandler handler, GroupState groupListener) {
    if (bufferedData.size() > 0) {
      Flow[] bufferedRecords = new Flow[bufferedData.size()];
      bufferedRecords = bufferedData.toArray(bufferedRecords);

      // Note that Arrays.sort is guaranteed to be stable for arrays of objects: 
      // equal elements will not be reordered as a result of the sort.
      Arrays.sort(bufferedRecords,comparator);

      //System.out.println("SortGroup.groupStopped recordCount (after sort) = " + bufferedRecords.length);

      int last = bufferedRecords.length - 1;

      Record previousRecord = firstRecord;
      for (int i = 0; i < bufferedRecords.length; ++i) {
        Flow flow = bufferedRecords[i];
        Record nextRecord = i < last ? bufferedRecords[i+1].getRecord() : lastRecord;
        child.writeRecord(context, flow,
          previousRecord, nextRecord, handler, groupListener);
        previousRecord = flow.getRecord();
      }
      bufferedData.clear();
      firstRecord = null;
      lastRecord = null;
    }                     
  }

  public void addToAttributes(ServiceContext context, Flow flow, Record variables, AttributesImpl attributes) {
  }

  public boolean isGrouping() {
    return bufferedData.size() == 0 ? false : true;
  }
}


