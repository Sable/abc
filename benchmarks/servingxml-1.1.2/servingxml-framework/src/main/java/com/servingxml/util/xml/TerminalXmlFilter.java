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

import java.io.IOException;

import javax.xml.transform.Templates;
import javax.xml.transform.URIResolver;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.DTDHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.XMLFilter;

/**
 *
 *  01/04/03
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class TerminalXmlFilter implements XMLFilter {

  private XMLReader parent = null;
  private ContentHandler handler = new NullContentHandler();
  private ContentHandler handler2;
  private EntityResolver entityResolver = null;
  private DTDHandler dtdHandler = null;
  private ErrorHandler errorHandler = null;

  public TerminalXmlFilter() {
  }

  public void setParent(XMLReader parent) {
    this.parent = parent;
  }

  public XMLReader getParent() {
    return parent;
  }

  public EntityResolver getEntityResolver() {
    return entityResolver;
  }

  public boolean getFeature(String name) throws SAXNotRecognizedException,SAXNotSupportedException {
    if (parent != null) {
        return parent.getFeature(name);
    } else {
        throw new SAXNotRecognizedException("Feature: " + name);
    }
  }
  public void setFeature(String name, boolean state) throws SAXNotRecognizedException,SAXNotSupportedException {
    if (parent != null) {
        parent.setFeature(name, state);
    } else {
        throw new SAXNotRecognizedException("Feature: " + name);
    }
  }

  public void setDTDHandler(DTDHandler handler) {
    if (handler == null) {
        throw new NullPointerException("Null DTD handler");
    } else {
        dtdHandler = handler;
    }
  }

  public Object getProperty(String name) throws SAXNotRecognizedException,SAXNotSupportedException {
    if (parent != null) {
        return parent.getProperty(name);
    } else {
        throw new SAXNotRecognizedException("Property: " + name);
    }
  }

  public void setProperty(String name, Object value) throws SAXNotRecognizedException,SAXNotSupportedException {
    if (parent != null) {
        parent.setProperty(name, value);
    } else {
        throw new SAXNotRecognizedException("Property: " + name);
    }
  }

  public void setEntityResolver(EntityResolver resolver) {
    this.entityResolver = resolver;
  }

  public ContentHandler getContentHandler() {
    return handler2;
  }

  public void setErrorHandler(ErrorHandler handler) {
    if (handler == null) {
        throw new NullPointerException("Null error handler");
    } else {
        errorHandler = handler;
    }
  }

  public DTDHandler getDTDHandler() {
    return dtdHandler;
  }

  public void setContentHandler(ContentHandler handler) {
    this.handler2 = handler;
  }

  public ErrorHandler getErrorHandler() {
    return errorHandler;
  }

  public void parse(InputSource inputSource) throws IOException,SAXException {
    if (parent != null) {
      parent.setContentHandler(handler);
      parent.parse(inputSource);
    }
  }

  public void parse(String systemId) throws IOException,SAXException {
    if (parent != null) {
      parent.setContentHandler(handler);
      parent.parse(systemId);
    }
  }
}

