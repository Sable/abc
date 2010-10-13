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

import org.xml.sax.helpers.AttributesImpl;

import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;
import com.servingxml.util.record.Record;
import com.servingxml.util.xml.ExtendedContentHandler;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class BufferedRecordMap implements MapXml {
  private final MapXml recordMap;
  private final List<RecordMapEvent> buffer;

  public BufferedRecordMap(MapXml recordMap) {
    this.recordMap = recordMap;
    this.buffer = new ArrayList<RecordMapEvent>();
  }

  public void addToAttributes(ServiceContext context, Flow flow, Record variables, AttributesImpl attributes) {
    recordMap.addToAttributes(context,flow,variables,attributes);
  }

  public void writeRecord(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord, 
  ExtendedContentHandler handler, GroupState groupListener) {
    //System.out.println(getClass().getName()+".writeRecord 1");
    buffer.add(new WriteRecordEvent(flow,previousRecord,nextRecord));
  }

  public void writeRecord(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord, 
  ExtendedContentHandler handler, GroupState groupListener, boolean grouping) {
    //System.out.println(getClass().getName()+".writeRecord 2 " + grouping);
    if (grouping) {
      writeRecord(context, flow, previousRecord, nextRecord, handler, groupListener);
    } else {
      if (buffer.size() > 0) {
        flush(context, handler, groupListener);
      }
      recordMap.writeRecord(context, flow, previousRecord, nextRecord, handler, groupListener);
    }
  }

  public void groupStarted(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord, 
  ExtendedContentHandler handler, Record variables) {
    StartRecordSetEvent event = new StartRecordSetEvent(flow,previousRecord,nextRecord,variables);
    buffer.add(event);
  }
  
  public void groupStopped(ServiceContext context, Flow flow, ExtendedContentHandler handler) {
    flush(context, handler, GroupState.DEFAULT);
    recordMap.groupStopped(context,flow,handler);
  }

  public boolean isGrouping() {
    return buffer.size() > 0 ? true : recordMap.isGrouping();
  }

  public void flush(ServiceContext context, ExtendedContentHandler handler, GroupState groupListener) {
    if (buffer.size() > 0) {
      int last = buffer.size() - 1;
      int stop = -1;
      for (int i = 0; i < buffer.size(); ++i) {
        RecordMapEvent event = buffer.get(i);
        if (stop == -1 && i > 1 && event.isStart()) {
          stop = i;
        }
        event.execute(recordMap, context, handler, groupListener, i);
      }
      if (stop == -1) {
        buffer.clear();
      } else {
        for (int i = 0; i < stop; ++i) {
          buffer.remove(0);
        }
      }
    }
  }

  interface RecordMapEvent {
    void execute(MapXml recordMap, ServiceContext context, ExtendedContentHandler handler, GroupState groupListener,
      int position);
    boolean isStart();
  }

  static class WriteRecordEvent implements RecordMapEvent {
    final Flow flow;
    final Record previousRecord;
    final Record nextRecord;

    WriteRecordEvent(Flow flow, Record previousRecord, Record nextRecord) {
      this.flow = flow;
      this.previousRecord = previousRecord;
      this.nextRecord = nextRecord;
    }

    public void execute(MapXml recordMap, ServiceContext context, ExtendedContentHandler handler, 
      GroupState groupListener, int position) {
      recordMap.writeRecord(context, flow, previousRecord, nextRecord, handler, groupListener);
    }

    public boolean isStart() {
      return false;
    }
  }

  static class StartRecordSetEvent implements RecordMapEvent {
    final Flow flow; 
    final Record previousRecord; 
    final Record nextRecord;
    final Record variables;

    StartRecordSetEvent(Flow flow, Record previousRecord, Record nextRecord, Record variables) {
      this.flow = flow;
      this.previousRecord = previousRecord;
      this.nextRecord = nextRecord;
      this.variables = variables;
    }

    public void execute(MapXml recordMap, ServiceContext context, ExtendedContentHandler handler, 
      GroupState groupListener, int position) {
      if (position == 0) {
        recordMap.groupStarted(context,flow,previousRecord,nextRecord,handler,variables);
      }
    }

    public boolean isStart() {
      return true;
    }
  }
}

