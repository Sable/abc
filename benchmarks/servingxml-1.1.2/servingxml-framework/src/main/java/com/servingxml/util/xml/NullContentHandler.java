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

import com.servingxml.util.PrefixMap;

public final class NullContentHandler implements ContentHandler {

  public NullContentHandler() {
  }

  public final void startDocument() {
  }

  public final void setDocumentLocator(Locator locator) {
  }

  public final void startPrefixMapping (String prefix, String uri)
  {
  }

  public final void endPrefixMapping (String prefix)
  {
  }

  public final void ignorableWhitespace (char ch[], int start, int length)
  {
  }
  
  public final void processingInstruction (String target, String data)
  {
  }
  public final void skippedEntity (String name)
  {
  }
  public final void startElement(String namespaceUri, String localName, String qname, 
  Attributes atts) {
  }
  public final void characters(char ch[], int start, int length) {
  }

  public final void endElement(String namespaceUri, String localName, String qname) {
  }

  public final void endDocument() {
  }
}

