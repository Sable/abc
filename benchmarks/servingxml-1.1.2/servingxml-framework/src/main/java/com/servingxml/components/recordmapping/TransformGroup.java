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

public class TransformGroup implements MapXml {                   
  private boolean started = false;
  private List<Flow> records = new ArrayList<Flow>();

  public void writeRecord(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord, 
  ExtendedContentHandler handler, GroupState groupListener) {
    if (started) {
      records.add(flow);
    }
  }

  public void groupStarted(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord, 
  ExtendedContentHandler handler, Record variables) {
    this.started = true;
    this.records.clear();
  }

  public void groupStopped(ServiceContext context, Flow flow, ExtendedContentHandler handler) {
    this.started = false;
  }

  public void addToAttributes(ServiceContext context, Flow flow, Record variables, AttributesImpl attributes) {
  }

  public boolean isGrouping() {
    return started;
  }
}

