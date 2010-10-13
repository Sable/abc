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

import java.io.IOException;
import java.util.Properties;
import java.util.Enumeration;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;

import com.servingxml.util.Name;
import com.servingxml.util.record.Record;
import com.servingxml.util.ServingXmlException;
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.ExpirableFamily;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.app.xmlpipeline.XmlFilterChain;

public class MultipleXmlFilter 
  implements XMLFilter, XmlFilterChain {
  private XMLFilter head;
  private XMLFilter tail;
  private final ExpirableFamily expirableFamily = new ExpirableFamily();
  private Properties outputProperties;

  public MultipleXmlFilter() {
  }

  public void addXmlFilter(XMLFilter filter) {
    filter.setParent(tail);
    if (head == null) {
      head = filter;
    } 
    tail = filter;
  }

  public void setParent(XMLReader parent) {
    if (head != null) {
      head.setParent(parent);
    }
  }

  public XMLReader getParent() {
    return head != null ? head.getParent() : null;
  }

  public void setFeature(String name, boolean value)
  throws SAXNotRecognizedException, SAXNotSupportedException {
    if (tail != null) {
      tail.setFeature(name, value);
    } else {
      throw new SAXNotRecognizedException("Feature: " + name);
    }
  }

  public boolean getFeature(String name)
  throws SAXNotRecognizedException, SAXNotSupportedException {
    if (tail != null) {
      return tail.getFeature(name);
    } else {
      throw new SAXNotRecognizedException("Feature: " + name);
    }
  }

  public void setProperty(String name, Object value)
  throws SAXNotRecognizedException, SAXNotSupportedException {
    if (tail != null) {
      tail.setProperty(name, value);
    } else {
      throw new SAXNotRecognizedException("Property: " + name);
    }
  }

  public Object getProperty(String name)
  throws SAXNotRecognizedException, SAXNotSupportedException {
    if (tail != null) {
      return tail.getProperty(name);
    } else {
      throw new SAXNotRecognizedException("Property: " + name);
    }
  }

  public void setEntityResolver(EntityResolver resolver) {
    tail.setEntityResolver(resolver);
  }

  public EntityResolver getEntityResolver () {
    return tail.getEntityResolver();
  }

  public void setDTDHandler(DTDHandler handler) {
    tail.setDTDHandler(handler);
  }

  public DTDHandler getDTDHandler() {
    return tail.getDTDHandler();
  }

  public void setContentHandler(ContentHandler handler) {
    tail.setContentHandler(handler);
  }

  public ContentHandler getContentHandler() {
    return tail.getContentHandler();
  }

  public void setErrorHandler(ErrorHandler handler) {
    tail.setErrorHandler(handler);
  }

  public ErrorHandler getErrorHandler() {
    return tail.getErrorHandler();
  }

  public void parse(InputSource input)
  throws SAXException, IOException {
    tail.parse(input);
  }

  public void parse(String systemId)
  throws SAXException, IOException {
    tail.parse(systemId);
  }

  public void setSaxSource(SaxSource saxSource) {
  }                                                                 


  public void addOutputProperties(Properties properties) {
    Enumeration enumer = properties.propertyNames();
    while (enumer.hasMoreElements()) {
      String name = (String)enumer.nextElement();
      this.outputProperties.setProperty(name,properties.getProperty(name));
    }
  }

  public Properties getOutputProperties() {
    return outputProperties;
  }

  public void addExpirable(Expirable expirable) {
    expirableFamily.addExpirable(expirable);
  }

  public XMLReader getXmlReader() {
    return head;
  }

  public XMLFilter getXmlFilter() {
    return head;
  }
}


