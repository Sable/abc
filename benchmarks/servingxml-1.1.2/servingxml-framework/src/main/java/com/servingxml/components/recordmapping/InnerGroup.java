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
import com.servingxml.util.record.RecordBuilder;
import com.servingxml.app.Environment;

/**
 * .
 * 
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class InnerGroup implements MapXml {

  private final Environment env;
  private final MapXml child;
  private final GroupRecognizer groupRecognizer;
  private boolean started = false;
  private Flow groupFlow;

  public InnerGroup(Environment env,
                    GroupRecognizer groupRecognizer, MapXml child) {

    this.env = env;
    this.child = child;
    this.groupRecognizer = groupRecognizer;
  }

  public void writeRecord(ServiceContext context, Flow flow, 
    Record previousRecord, Record nextRecord, 
    ExtendedContentHandler handler, GroupState groupListener) {


    //System.out.println(getClass().getName()+".writeRecord enter");

    Record record = flow.getRecord();
    //System.out.println(getClass().getName()+" "+record.toXmlString(context));
    boolean doEnd = false;
    if (!started) {
      boolean doStart = groupRecognizer.startRecognized(context, flow, previousRecord, record);
      if (doStart) {
        //System.out.println(getClass().getName()+"writeRecord Making group parameters \n"+record.toXmlString(context));
        groupFlow = env.augmentParametersOf(context,flow);

        groupStopped(context, groupFlow, handler);
        child.groupStarted(context, groupFlow, previousRecord,nextRecord,handler,Record.EMPTY);
        started = true;
      }
      //System.out.println("InnerGroup.writeRecord !started doStart = " + doStart);
    }

    if (started) {
      groupListener.startGroup();
      if (nextRecord == null || groupRecognizer.endRecognized(context,flow,record,nextRecord)) {
        doEnd = true;
      }
      //System.out.println("InnerGroup.writeRecord started doEnd = " + doEnd);
    }
    if (started) {
      //System.out.println("InnerGroup.writeRecord replacing parameters with "+groupFlow.getParameters().toXmlString(context));
      Flow newFlow = flow.replaceParameters(context,groupFlow.getParameters());
      child.writeRecord(context, newFlow, previousRecord, nextRecord, handler, groupListener);
    }
    if (doEnd) {
      //System.out.println("InnerGroup.writeRecord doEnd");
      groupFlow = env.augmentParametersOf(context,flow);
      groupStopped(context, groupFlow, handler);
      groupListener.endGroup();
    }
    //System.out.println("end InnerGroup.writeRecord started = " + started);
    //System.out.println(getClass().getName()+".writeRecord leave");
  }

  public void groupStarted(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord, 
    ExtendedContentHandler handler, Record variables) {
  }

  public void groupStopped(ServiceContext context, Flow flow, ExtendedContentHandler handler) {
    //System.out.println(getClass().getName()+".groupStopped enter");
    Flow newFlow = flow;
    if (started) {
      newFlow = env.augmentParametersOf(context,flow);
      child.groupStopped(context, newFlow, handler);
      started = false;
    }
    //System.out.println(getClass().getName()+".groupStopped leave");
  }

  public void addToAttributes(ServiceContext context, Flow flow, Record variables, AttributesImpl attributes) {
  }

  public boolean isGrouping() {
    return started;
  }
}

