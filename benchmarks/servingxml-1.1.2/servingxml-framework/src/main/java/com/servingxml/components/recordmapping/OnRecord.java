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

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.Value;
import com.servingxml.util.xml.ExtendedContentHandler;
import com.servingxml.components.recordio.RecordAccepter;

/**
 * A command for mapping a record in a flat file into an XML stream.
 * 
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */                                                             

public class OnRecord implements MapXml {

  private final RecordAccepter accepter;
  private final MapXml children;

  public OnRecord(RecordAccepter accepter, MapXml children) {
    this.accepter = accepter;
    this.children = children;   
  }

  public void writeRecord(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord, 
    ExtendedContentHandler handler, GroupState groupListener) {
    Record record = flow.getRecord();

    if (accepter.accept(context, flow, Value.EMPTY)) {
      //System.out.println(getClass().getName()+".writeRecord "+Integer.toString(flow.getCurrentLineNumber()));
      groupListener.startGroup();
      children.groupStarted(context, flow, previousRecord, nextRecord, handler, Record.EMPTY);
      children.groupStopped(context, flow, handler);
      groupListener.endGroup();
    }
  }

  public void groupStarted(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord, 
    ExtendedContentHandler handler, Record variables) {
  }                                                

  public void groupStopped(ServiceContext context, Flow flow, ExtendedContentHandler handler) {
  }

  public void addToAttributes(ServiceContext context, Flow flow, Record variables, AttributesImpl attributes) {
  }

  public boolean isGrouping() {
    return false;
  }
}                                  
