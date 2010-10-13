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

package com.servingxml.util.xml;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.servingxml.util.PrefixMap;

public abstract class ContentHandlerFilter implements ContentHandler {
  private ContentHandler handler;

  public ContentHandlerFilter() {
    this.handler = null;
  }

  public ContentHandlerFilter(ContentHandler handler) {
    this.handler = handler;
  }

  public ContentHandler getContentHandler() {
    return handler;
  }

  public void setContentHandler(ContentHandler handler) {
    this.handler = handler;
  }

  public void startDocument() throws SAXException {
    if (handler != null) {
      handler.startDocument();
    }
  }

  public void setDocumentLocator(Locator locator) {
    if (handler != null) {
      handler.setDocumentLocator(locator);
    }
  }

  public void startPrefixMapping (String prefix, String uri)
  throws SAXException {
    if (handler != null) {
      handler.startPrefixMapping(prefix,uri);
    }
  }

  public void endPrefixMapping (String prefix)
  throws SAXException {
    if (handler != null) {
      handler.endPrefixMapping(prefix);
    }
  }

  public void ignorableWhitespace (char ch[], int start, int length)
  throws SAXException {
    if (handler != null) {
      handler.ignorableWhitespace(ch,start,length);
    }
  }

  public void processingInstruction (String target, String data)
  throws SAXException {
    if (handler != null) {
      handler.processingInstruction(target,data);
    }
  }

  public void skippedEntity (String name)
  throws SAXException {
    if (handler != null) {
      handler.skippedEntity(name);
    }
  }

  public void startElement(String namespaceUri, String localName, String qname, 
                           Attributes atts) throws SAXException {
    if (handler != null) {
      handler.startElement(namespaceUri,localName,qname,atts);
    }
  }

  public void characters(char ch[], int start, int length) throws SAXException {
    if (handler != null) {
      handler.characters(ch,start,length);
    }
  }

  public void endElement(String namespaceUri, String localName, String qname) throws SAXException {
    if (handler != null) {
      handler.endElement(namespaceUri,localName,qname);
    }
  }

  public void endDocument() throws SAXException {
    if (handler != null) {
      handler.endDocument();
    }
  }
}

