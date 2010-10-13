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

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.servingxml.util.Name;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;
import com.servingxml.util.record.Record;
import com.servingxml.util.ServingXmlException;
import com.servingxml.components.string.Stringable;
import com.servingxml.util.xml.ExtendedContentHandler;

/**
 * A command for inserting an element mapped from a flat file into an XML stream.
 * 
 * 
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class GenerateCData implements MapXml {

  private final Stringable[] stringFactories;
  private final MapXml children;

  private boolean started = false;

  public GenerateCData(Stringable[] stringFactories, MapXml children) {

    this.stringFactories = stringFactories;
    this.children = children;
  }

  public void writeRecord(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord, 
                          ExtendedContentHandler handler, GroupState groupListener) {
    children.writeRecord(context, flow,previousRecord,nextRecord,handler,groupListener);
  }                                         

  public void groupStarted(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord, 
                           ExtendedContentHandler handler, Record variables) {
    if (!started) {

      started = true;
      try {
        handler.startCDATA();
        for (int i = 0; i < stringFactories.length; ++i) {
          Stringable stringFactory = stringFactories[i];
          String value = stringFactory.createString(context, flow);
          if (value.length() > 0) {
            handler.characters(value.toCharArray(),0,value.length());
          }
        }
        children.groupStarted(context, flow, previousRecord,nextRecord, handler, Record.EMPTY);
      } catch (SAXException e) {
        throw new ServingXmlException(e.getMessage(), e);
      }
    }
  }

  public void groupStopped(ServiceContext context, Flow flow, ExtendedContentHandler handler) {
    children.groupStopped(context, flow, handler);
    try {
      if (started) {
        handler.endCDATA();
        started = false;
      }
    } catch (SAXException e) {
      throw new ServingXmlException(e.getMessage(), e);
    }
  }

  public void addToAttributes(ServiceContext context, Flow flow, Record variables, AttributesImpl attributes) {
  }

  public boolean isGrouping() {
    return started;
  }
}




