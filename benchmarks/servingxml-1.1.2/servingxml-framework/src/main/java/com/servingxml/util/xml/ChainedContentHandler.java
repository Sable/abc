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

public abstract class ChainedContentHandler implements ContentHandler {
  private ContentHandler contentHandler;

  public ChainedContentHandler() {
    this.contentHandler = null;
  }

  public ChainedContentHandler(ContentHandler contentHandler) {
    this.contentHandler = contentHandler;
  }

  public void startDocument() throws SAXException {
    if (contentHandler != null) {
      contentHandler.startDocument();
    }
  }

  public void setDocumentLocator(Locator locator) {
    if (contentHandler != null) {
      contentHandler.setDocumentLocator(locator);
    }
  }

  public void startPrefixMapping (String prefix, String uri)
  throws SAXException {
    if (contentHandler != null) {
      //System.out.println(getClass().getName()+".startPrefixMapping " + prefix + ":" + uri);
      contentHandler.startPrefixMapping(prefix,uri);
    }
  }

  public void endPrefixMapping (String prefix)
  throws SAXException {
    if (contentHandler != null) {
      //System.out.println(getClass().getName()+".endPrefixMapping " + prefix);
      contentHandler.endPrefixMapping(prefix);
    }
  }

  public void ignorableWhitespace (char ch[], int start, int length)
  throws SAXException {
    if (contentHandler != null) {
      contentHandler.ignorableWhitespace(ch,start,length);
    }
  }

  public void processingInstruction (String target, String data)
  throws SAXException {
    if (contentHandler != null) {
      contentHandler.processingInstruction(target,data);
    }
  }

  public void skippedEntity (String name)
  throws SAXException {
    if (contentHandler != null) {
      contentHandler.skippedEntity(name);
    }
  }

  public void startElement(String namespaceUri, String localName, String qname, 
    Attributes atts) throws SAXException {
    if (contentHandler != null) {
      contentHandler.startElement(namespaceUri,localName,qname,atts);
    }

  }

  public void characters(char ch[], int start, int length) throws SAXException {
    if (contentHandler != null) {
      contentHandler.characters(ch,start,length);
    }
  }

  public void endElement(String namespaceUri, String localName, String qname) throws SAXException {
    if (contentHandler != null) {
      contentHandler.endElement(namespaceUri,localName,qname);
    }
  }

  public void endDocument() throws SAXException {
    if (contentHandler != null) {
      contentHandler.endDocument();
    }
  }

  public void setContentHandler(ContentHandler contentHandler) {
    this.contentHandler = contentHandler;
  }

  public ContentHandler getContentHandler() {
    return contentHandler;
  }
}

