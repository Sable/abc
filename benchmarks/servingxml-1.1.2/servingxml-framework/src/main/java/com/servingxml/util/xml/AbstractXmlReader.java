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
import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

/**
 * A <code>AbstractXmlReader</code> implement a SAX 2 <code>XMLReader</code> interface
 *
 * 
 * @author  Daniel A. Parker
 */

public abstract class AbstractXmlReader implements XMLReader {

  public static final Attributes EMPTY_ATTRIBUTES = new AttributesImpl();

  protected static final String NAMESPACE_PREFIXES_FEATURE = "http://xml.org/sax/features/namespace-prefixes";
  protected static final String NAMESPACES_FEATURE = "http://xml.org/sax/features/namespaces";

  protected ContentHandler contentHandler;
  private EntityResolver entityResolver = null;
  private DTDHandler dtdHandler = null;
  private ErrorHandler errorHandler = null;
  private boolean namespaces = true;
  private boolean namespacePrefixes = false;
  private final HashMap<String,String> prefixMap = new HashMap<String,String>();
  private int lastPrefixIndex = 0;

  public boolean getFeature(String name)
  throws SAXNotRecognizedException, SAXNotSupportedException {

    boolean value = false;
    if (name.equals(NAMESPACES_FEATURE)) {
      value = namespaces;
    } else if (name.equals(NAMESPACE_PREFIXES_FEATURE)) {
      value = namespacePrefixes;
    } else {
      throw new SAXNotRecognizedException(name + " not recognized");
    }

    return value;
  }

  public void setFeature(String name, boolean value)
  throws SAXNotRecognizedException, SAXNotSupportedException {
    if (name.equals(NAMESPACES_FEATURE)) {
      namespaces = value;
    } else if (name.equals(NAMESPACE_PREFIXES_FEATURE)) {
        namespacePrefixes = value;
    } else {
      throw new SAXNotRecognizedException(name + " not recognized");
    }
  }
  public Object getProperty(String name)
  throws SAXNotRecognizedException, SAXNotSupportedException {
    return null;
  }

  public void setProperty(String name, Object value)
  throws SAXNotRecognizedException, SAXNotSupportedException {
  }

  public void setEntityResolver(EntityResolver resolver) {
    this.entityResolver = resolver;
  }

  public EntityResolver getEntityResolver() {
    return entityResolver;
  }

  public void setDTDHandler(DTDHandler handler) {
    this.dtdHandler = handler;
  }
  public DTDHandler getDTDHandler() {
    return dtdHandler;
  }
  public ContentHandler getContentHandler() {
    return contentHandler;
  }

  public void setContentHandler(ContentHandler handler) {

    this.contentHandler = handler;
  }
  public void setErrorHandler(ErrorHandler handler) {
    this.errorHandler = handler;
  }
  public ErrorHandler getErrorHandler() {
    return errorHandler;
  }

  public void parse(InputSource input) throws IOException, SAXException {
    parse(input.getSystemId());
  }

  public abstract void parse(String systemId)
  throws IOException, SAXException;

  public String generateQname(String namespaceUri, String localName) {
    String qname;
    if (namespaceUri == null || namespaceUri.length() == 0) {
      qname = localName;
    } else {
      String prefix = prefixMap.get(namespaceUri);
      if (prefix == null) {
        prefix = "ns" + ++lastPrefixIndex + ":";
        prefixMap.put(namespaceUri,prefix);
      }
      qname = prefix + localName;
    }
    return qname;
  }
}
