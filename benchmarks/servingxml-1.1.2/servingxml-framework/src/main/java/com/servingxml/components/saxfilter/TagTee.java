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

package com.servingxml.components.saxfilter;

import java.util.Properties;
import java.util.Enumeration;
import java.io.IOException;

import org.xml.sax.InputSource;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import com.servingxml.io.cache.ExpirableFamily;
import com.servingxml.app.xmlpipeline.XmlFilterChain;
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.io.saxsink.SaxSink;

public class TagTee extends XMLFilterImpl implements XMLFilter, XmlFilterChain {
  private Properties outputProperties = new Properties();
  private final SaxSink saxSink;
  private XMLFilterImpl headFilter;
  private XMLFilter tailFilter;

  public TagTee(SaxSink saxSink) {
    this.saxSink = saxSink;
    this.headFilter = new MyFilter();
    this.tailFilter = headFilter;
  }

  public void startDocument() throws SAXException {
    super.startDocument();
    tailFilter.setContentHandler(saxSink.getContentHandler());
    try {
      tailFilter.parse("");
    } catch (IOException e) {
    }
    headFilter.startDocument();
  }

  public void endDocument() throws SAXException {
    super.endDocument();
    headFilter.endDocument();
    saxSink.close();
  }

  public void startElement(String namespaceUri, String localName, String qname, Attributes atts)
  throws SAXException {
    super.startElement(namespaceUri,localName,qname,atts);
    if (headFilter != null) {
      headFilter.startElement(namespaceUri,localName,qname,atts);
    }
    //Sytem.out.println(qname);
  }

  public void endElement(String namespaceUri, String localName, String qname)
  throws SAXException {
    super.endElement(namespaceUri,localName,qname);
    if (headFilter != null) {
      headFilter.endElement(namespaceUri,localName,qname);
    }
    //Sytem.out.println(qname);
  }

  public void setDocumentLocator(Locator locator) {
    super.setDocumentLocator(locator);
    if (headFilter != null) {
      headFilter.setDocumentLocator(locator);
    }
  }

  public void startPrefixMapping (String prefix, String uri)
  throws SAXException {
    super.startPrefixMapping(prefix,uri);
    if (headFilter != null) {
      headFilter.startPrefixMapping(prefix,uri);
    }
  }

  public void endPrefixMapping (String prefix)
  throws SAXException {

    super.endPrefixMapping(prefix);
    if (headFilter != null) {
      headFilter.endPrefixMapping(prefix);
    }
  }

  public void ignorableWhitespace (char ch[], int start, int length)
  throws SAXException {
    super.ignorableWhitespace(ch,start,length);
    if (headFilter != null) {
      headFilter.ignorableWhitespace(ch,start,length);
    }
  }

  public void processingInstruction (String target, String data)
  throws SAXException {
    super.processingInstruction(target,data);
    if (headFilter != null) {
      headFilter.processingInstruction(target,data);
    }
  }

  public void skippedEntity (String name)
  throws SAXException {
    super.skippedEntity(name);
    if (headFilter != null) {
      headFilter.skippedEntity(name);
    }
  }

  public void characters(char[] ch, int start, int length) 
  throws SAXException {
    super.characters(ch,start,length);
    if (headFilter != null) {
      headFilter.characters(ch,start,length);
    }
  }

  //  XmlFilterChain

  public void setSaxSource(SaxSource saxSource) {
  }

  public XMLReader getXmlReader() {
    return this; 
  }

  public void addXmlFilter(XMLFilter filter) {
    filter.setParent(tailFilter);
    tailFilter = filter;
  }

  public Properties getOutputProperties() {
    return outputProperties;
  }

  public void addOutputProperties(Properties properties) {
    Enumeration enumer = properties.propertyNames();
    while (enumer.hasMoreElements()) {
      String name = (String)enumer.nextElement();
      this.outputProperties.setProperty(name,properties.getProperty(name));
    }
  }

  public Expirable getExpirable() {
    return Expirable.IMMEDIATE_EXPIRY;
  }

  public void addExpirable(Expirable expirable) {
  }

  static class MyFilter extends XMLFilterImpl {
    public void parse (InputSource input)
    throws SAXException, IOException
    {
    }
    public void parse (String systemId)
    throws SAXException, IOException
    {
    }
  }
}


