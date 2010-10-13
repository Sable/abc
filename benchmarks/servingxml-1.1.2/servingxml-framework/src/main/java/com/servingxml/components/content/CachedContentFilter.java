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

package com.servingxml.components.content;

import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.servingxml.util.ServingXmlException;
import com.servingxml.components.saxsource.SaxEventBuffer;
import com.servingxml.components.saxsource.SaxEventBufferBuilder;

public class CachedContentFilter extends XMLFilterImpl {

  private final CachedContentCommand cacheCommand;

  private SaxEventBufferBuilder cachingContentHandler = new SaxEventBufferBuilder();

  public CachedContentFilter(CachedContentCommand cacheCommand) {
    this.cacheCommand = cacheCommand;
  }

  public void startDocument() throws SAXException {
    super.startDocument();
    cachingContentHandler.startDocument();
  }

  public void startPrefixMapping (String prefix, String uri)
  throws SAXException {
    super.startPrefixMapping(prefix,uri);
    cachingContentHandler.startPrefixMapping(prefix,uri);
  }

  public void endPrefixMapping (String prefix)
  throws SAXException {
    super.endPrefixMapping(prefix);
    cachingContentHandler.endPrefixMapping(prefix);
  }

  public void ignorableWhitespace (char ch[], int start, int length)
  throws SAXException {
    super.ignorableWhitespace(ch, start, length);
    cachingContentHandler.ignorableWhitespace(ch, start, length);
  }
  
  public void processingInstruction (String target, String data)
  throws SAXException {
    super.processingInstruction(target, data);
    cachingContentHandler.processingInstruction(target, data);
  }
  public void skippedEntity (String name)
  throws SAXException {
    super.skippedEntity(name);
    cachingContentHandler.skippedEntity(name);
  }
  public void startElement(String namespaceUri, String localName, String qname, 
  Attributes atts) throws SAXException {
    super.startElement(namespaceUri,localName,qname,atts);
    cachingContentHandler.startElement(namespaceUri,localName,qname,atts);
  }
  public void characters(char ch[], int start, int length) throws SAXException {
    super.characters(ch,start,length);
    cachingContentHandler.characters(ch,start,length);
  }
  public void endElement(String namespaceUri, String localName, String qname) throws SAXException {
    super.endElement(namespaceUri,localName,qname);
    cachingContentHandler.endElement(namespaceUri,localName,qname);
  }

  public void endDocument() throws SAXException {
    super.endDocument();
    cachingContentHandler.endDocument();
  }

  public void parse(InputSource input) throws SAXException, IOException {
    super.parse(input);
    afterParse();
  }
  public void parse(String systemId) throws SAXException, IOException {
    super.parse(systemId);
    afterParse();
  }

  private void afterParse() throws IOException,SAXException { 
    try {
      SaxEventBuffer saxEventBuffer = cachingContentHandler.getBuffer();
      cacheCommand.execute(saxEventBuffer);
    } catch (ServingXmlException e) {
      throw new SAXException(e.getMessage(),e);
    }
  }
}

