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

import com.servingxml.util.SystemConstants;

public class XsltChooseReceiver implements ContentHandler {

  private int index = -1;

  public XsltChooseReceiver() {
  }

  public int getSelection() {
    return index;
  }

  public void startDocument() throws SAXException {
  }

  public void setDocumentLocator(Locator locator) {
  }

  public void startPrefixMapping (String prefix, String uri)
  throws SAXException {
  }

  public void endPrefixMapping (String prefix)
  throws SAXException {
  }

  public void ignorableWhitespace (char ch[], int start, int length)
  throws SAXException {
  }

  public void processingInstruction (String target, String data)
  throws SAXException {
  }

  public void skippedEntity (String name)
  throws SAXException {
  }

  public void startElement(String namespaceUri, String localName, String qname, 
                           Attributes atts) throws SAXException {

    if (namespaceUri.equals(SystemConstants.SERVINGXML_NS_URI) && localName.equals("result")) {
      String value = atts.getValue("","index");
      if (value != null && value.length() > 0) {
        index = Integer.parseInt(value);
      }
    }
  }

  public void characters(char ch[], int start, int length) throws SAXException {
  }

  public void endElement(String namespaceUri, String localName, String qname) throws SAXException {
  }

  public void endDocument() throws SAXException {
  }
}

