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
import com.servingxml.util.Name;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.Value;
import com.servingxml.util.xml.ExtendedContentHandler;

/**
 * A command for mapping a record in a flat file into an XML stream.
 * 
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */                                                             

public class RepeatingGroupMap implements MapXml {

  private final Name fieldName;
  private final MapXml children;

  public RepeatingGroupMap(Name fieldName, MapXml children) {
    this.fieldName = fieldName;
    this.children = children;   
  }

  public void writeRecord(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord, 
                          ExtendedContentHandler handler, GroupState groupListener) {
  }

  public void groupStarted(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord, 
                           ExtendedContentHandler handler, Record variables) {
    Record record = flow.getRecord();

    Value value = record.getValue(fieldName);

    if (value != null) {
      Record[] subrecords = value.getRecords();
      children.groupStarted(context, flow, previousRecord, nextRecord, handler, Record.EMPTY);
      for (int i = 0; i < subrecords.length; ++i) {
        Record subrecord = subrecords[i];
        Flow newFlow = flow.replaceRecord(context, subrecord);
        children.writeRecord(context,newFlow,previousRecord,nextRecord,handler,GroupState.DEFAULT);
      }
      children.groupStopped(context, flow, handler);
    }
  }                                                

  public void groupStopped(ServiceContext context, Flow flow, ExtendedContentHandler handler) {
  }

  public void addToAttributes(ServiceContext context, Flow flow, Record variables, AttributesImpl attributes) {
  }

  public boolean isGrouping() {
    return false;
  }
}                                  
