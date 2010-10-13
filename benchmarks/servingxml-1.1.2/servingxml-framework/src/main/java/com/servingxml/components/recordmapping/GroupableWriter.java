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
import java.util.List;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.util.record.Record;
import com.servingxml.util.xml.ExtendedContentHandler;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */


interface GroupableWriter {
  void writeRecord(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord, 
    ExtendedContentHandler handler, GroupState groupListener);
  GroupableWriterStack getStack();
  MapXml[] getSiblings();
}

class DefaultGroupableWriter implements GroupableWriter {
  private final GroupableWriterStack writerStack;
  private final MapXml[] siblings;
  private final GroupableWriter head;

  DefaultGroupableWriter(GroupableWriterStack writerStack, MapXml[] siblings) {
    this.writerStack = writerStack;
    this.siblings = siblings;
    this.head = this;
  }

  DefaultGroupableWriter(GroupableWriterStack writerStack, MapXml[] siblings, GroupableWriter head) {
    this.writerStack = writerStack;
    this.siblings = siblings;
    this.head = head;
  }

  public void writeRecord(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord, 
    ExtendedContentHandler handler, GroupState groupListener) {
    boolean done = false;
    for (int i = 0; !done && i < siblings.length; ++i) {
      MapXml child = siblings[i];
      //System.out.println(getClass().getName()+".writeRecord child is " + child.getClass().getName() + ", started=" + gl.wasStarted() + ", grouping=" + gl.isGrouping());
      child.writeRecord(context, flow, previousRecord,nextRecord, handler, groupListener);
      if (child.isGrouping()) {
        if (siblings.length > 1) {
          MapXml[] others = new MapXml[siblings.length-1];
          int index = 0;
          for (int j = 0; j < siblings.length; ++j) {
            if (j != i) {
              others[index++] = siblings[j];
            }
          }
          GroupableWriter active = new ActiveGroupableWriter(child, others, flow.getRecord(), head);
          writerStack.setActive(active);
        }
        done = true;
      }
    }
  }
  public MapXml[] getSiblings() {
    return siblings;
  }
  public GroupableWriterStack getStack() {
    return writerStack;
  }
}

class ActiveGroupableWriter implements GroupableWriter {
  private final MapXml activeSibling;
  private final MapXml[] others;
  private final Record lastRecord;
  private List<Flow> history = new ArrayList<Flow>();
  private final GroupableWriter head;
  private final ActiveGroupableWriter tail;

  public ActiveGroupableWriter(MapXml activeSibling, MapXml[] others, Record lastRecord, GroupableWriter head) {
    this.activeSibling = activeSibling;
    this.others = others;
    this.lastRecord = lastRecord;
    this.head = head;
    this.tail = null;
  }

  public ActiveGroupableWriter(MapXml activeSibling, MapXml[] others, Record lastRecord, GroupableWriter head, ActiveGroupableWriter tail) {
    this.activeSibling = activeSibling;
    this.others = others;
    this.lastRecord = lastRecord;
    this.head = head;
    this.tail = tail;
  }

  public void writeRecord(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord, 
    ExtendedContentHandler handler, GroupState groupListener) {
    activeSibling.writeRecord(context,flow,previousRecord,nextRecord,handler,groupListener);
    history.add(flow);
    if (!activeSibling.isGrouping()) {
      getStack().setActive(null);
      writeHistory(context,nextRecord,handler,groupListener);
      if (getStack().getActive() != null) {
        ActiveGroupableWriter tail = getStack().getActive();
        getStack().setActive(new ActiveGroupableWriter(tail.getActiveSibling(), head.getSiblings(), flow.getRecord(), head, tail));
      }
    }
  }

  public void writeHistory(ServiceContext context, Record currentRecord, 
    ExtendedContentHandler handler, GroupState groupListener) {
    if (tail != null) {
      Record next = history.size() > 0 ? history.get(0).getRecord() : currentRecord;
      writeHistory(context,next,handler,groupListener);
    }
    getStack().pushDefault(new DefaultGroupableWriter(getStack(), others, head));
    Record previous = lastRecord;
    int last = history.size()-1;
    for (int i = 0; i < history.size(); ++i) {
      Flow currentFlow = history.get(i);
      Record next = i < last ? history.get(i+1).getRecord() : currentRecord; 
      getStack().writeRecord(context,currentFlow,previous,next,handler,groupListener);
    }
    history.clear();
    getStack().popDefault();
  }

  public MapXml getActiveSibling() {
    return activeSibling;
  }

  public GroupableWriterStack getStack() {
    return head.getStack();
  }

  public MapXml[] getSiblings() {
    return others;
  }
}

interface GroupableWriterStack {
  ActiveGroupableWriter getActive();
  void setActive(GroupableWriter active);
  void pushDefault(GroupableWriter active);
  GroupableWriter popDefault();
  void writeRecord(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord, 
    ExtendedContentHandler handler, GroupState groupListener);
}

