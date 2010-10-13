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
import com.servingxml.app.Flow;
import com.servingxml.util.record.Record;
import com.servingxml.util.xml.ExtendedContentHandler;
import com.servingxml.app.ParameterDescriptor;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class RecordMapPrefilter implements MapXml {
  private final MapXml recordMap;
  private final ParameterDescriptor[] parameterDescriptors;

  public RecordMapPrefilter(MapXml recordMap,
    ParameterDescriptor[] parameterDescriptors) {
    this.recordMap = recordMap;
    this.parameterDescriptors = parameterDescriptors;
  }

  public void writeRecord(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord, 
    ExtendedContentHandler handler, GroupState groupListener) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    //System.out.println(getClass().getName()+".writeRecord " + newParameters.toXmlString(context));
    recordMap.writeRecord(context,newFlow,previousRecord,nextRecord,handler,groupListener);
  }

  public void groupStarted(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord, 
    ExtendedContentHandler handler, Record variables) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    recordMap.groupStarted(context,newFlow,previousRecord,nextRecord,handler,Record.EMPTY);
  }

  public void groupStopped(ServiceContext context, Flow flow, ExtendedContentHandler handler) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    recordMap.groupStopped(context, newFlow, handler);
  }

  public void addToAttributes(ServiceContext context, Flow flow, Record variables, AttributesImpl attributes) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    recordMap.addToAttributes(context, newFlow, variables, attributes);
  }

  public boolean isGrouping() {
    return recordMap.isGrouping();
  }
}

