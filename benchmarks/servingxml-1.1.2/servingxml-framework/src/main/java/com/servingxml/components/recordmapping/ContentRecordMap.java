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

import java.io.IOException;
import org.xml.sax.helpers.AttributesImpl;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.content.Content;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;
import com.servingxml.util.xml.XmlSubtreeReader;
import com.servingxml.util.xml.ExtendedContentHandler;
import com.servingxml.io.saxsource.SaxSource;
/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ContentRecordMap implements MapXml {
  private final Content[] contentFactories;

  public ContentRecordMap(Content[] contentFactories) {
    this.contentFactories = contentFactories;
  }

  public void writeRecord(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord,
  ExtendedContentHandler handler, GroupState groupListener) {
  }

  public void groupStarted(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord, 
  ExtendedContentHandler handler, Record variables) {
    for (int i = 0; i < contentFactories.length; ++i) {
      try {
        SaxSource saxSource = contentFactories[i].createSaxSource(context, flow);
        XMLReader reader = new XmlSubtreeReader(saxSource.createXmlReader());
        reader.setContentHandler(handler);
        reader.parse("");
      } catch (ServingXmlException e) {
        throw e;
      } catch (Exception e) {
        throw new ServingXmlException(e.getMessage(), e);
      }                                                                 
    }
  }

  public void groupStopped(ServiceContext context, Flow flow, ExtendedContentHandler handler) {
  }

  public void addToAttributes(ServiceContext context, Flow flow, Record variables, 
    AttributesImpl attributes) {
  }

  public boolean isGrouping() {
    return false;
  }
}

