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

package com.servingxml.components.inverserecordmapping;

import org.xml.sax.SAXException;

import com.servingxml.app.ServiceContext;
import com.servingxml.expr.saxpath.SaxPath;
import com.servingxml.app.Flow;
import com.servingxml.components.recordio.RecordWriter;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class MultipleXmlFlattener implements ShredXml {

  private final ShredXml[] flatteners;

  public MultipleXmlFlattener(ShredXml[] flatteners) {
    this.flatteners = flatteners;
  }

  public boolean isMatched() {
    boolean matched = false;
    for (int i = 0; !matched && i < flatteners.length; ++i) {
      if (flatteners[i].isMatched()) {
        matched = true;
      }
    }
    return matched;
  }

  public void matchPath(ServiceContext context, Flow flow, SaxPath path) 
  throws SAXException {
    for (int i = 0; i < flatteners.length; ++i) {
      flatteners[i].matchPath(context, flow, path);
    }

  }

  public void startElement(ServiceContext context, Flow flow, SaxPath path,
    RecordWriter recordWriter) 
  throws SAXException {
    for (int i = 0; i < flatteners.length; ++i) {
      flatteners[i].startElement(context, flow, path, recordWriter);
    }

  }

  public void characters(char ch[], int start, int length) throws SAXException {
    for (int i = 0; i < flatteners.length; ++i) {
      flatteners[i].characters(ch, start, length);
    }
  }

  public void endElement(ServiceContext context, Flow flow,
    String namespaceUri, String localName, String qname, RecordWriter recordWriter)
  throws SAXException {
    for (int i = 0; i < flatteners.length; ++i) {
      flatteners[i].endElement(context, flow, namespaceUri, localName, qname, recordWriter);
    }
  }

  public final void mapRecord(ServiceContext context, Flow flow, RecordWriter recordWriter)
  throws SAXException {
    for (int i = 0; i < flatteners.length; ++i) {
      flatteners[i].mapRecord(context, flow, recordWriter);
    }
  }
}

