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

import org.xml.sax.helpers.AttributesImpl;

import com.servingxml.app.ServiceContext;
import com.servingxml.util.record.Record;
import com.servingxml.app.Flow;
import com.servingxml.util.xml.ExtendedContentHandler;
import com.servingxml.app.Environment;

/**
 * Implements a factory class for creating <tt>OnGroupToXmlHandler</tt> instances.
 * 
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class GroupBy implements MapXml {

  private final Environment env;
  private final MapXml child;
  private final GroupRecognizer groupRecognizer;
  private boolean started = false;
  private Flow groupFlow;

  public GroupBy(Environment env,
                 GroupRecognizer groupRecognizer, MapXml child) {
    this.env = env;
    this.child = child;
    this.groupRecognizer = groupRecognizer;
  }

  public void writeRecord(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord, 
    ExtendedContentHandler handler, GroupState groupListener) {
    Record record = flow.getRecord();

    if (!started) {
      //boolean doStart = groupRecognizer.startRecognized(context, flow, previousRecord, record);
      //if (doStart) {
        groupFlow = env.augmentParametersOf(context,flow);
        groupStopped(context, groupFlow, handler);
        child.groupStarted(context, groupFlow, previousRecord,nextRecord,handler,Record.EMPTY);
        started = true;
      //}
    }

    if (started) {
      groupListener.startGroup();
      Flow newFlow = flow.replaceParameters(context,groupFlow.getParameters());
      child.writeRecord(context, newFlow, previousRecord, nextRecord, handler, groupListener);
      if (nextRecord == null || groupRecognizer.endRecognized(context, flow, record,nextRecord)) {
        groupListener.endGroup();
        groupStopped(context, groupFlow, handler);
      }
    } else {
      child.writeRecord(context, flow, previousRecord, nextRecord, handler, groupListener);
    }
  }

  public void groupStarted(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord, 
    ExtendedContentHandler handler, Record variables) {
  }


  public void groupStopped(ServiceContext context, Flow flow, ExtendedContentHandler handler) {
    //System.out.println("GroupBy.groupStopped started = " + started);
    child.groupStopped(context, flow, handler);
    if (started) {
      started = false;
    }
  }

  public void addToAttributes(ServiceContext context, Flow flow, Record variables, AttributesImpl attributes) {
  }

  public boolean isGrouping() {
    return started;
  }
}

