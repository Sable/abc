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

package com.servingxml.components.content.dynamic;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.io.IOException;

import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.EntityResolver;
import org.xml.sax.DTDHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXNotRecognizedException;

import com.servingxml.util.Asserter;
import com.servingxml.app.ServiceContext;
import com.servingxml.util.ServingXmlException;

/**
 * A <code>DynamicContentReader</code> implement a SAX 2 <code>XMLReader</code> interface for feeding
 * start/end elements back to an application requesting an XML document.
 *
 * 
 * @author  Daniel A. Parker
 */

public class DynamicContentReader implements XMLReader {
  private static final String sourceClass = DynamicContentReader.class.getName();

  private final DynamicContentHandler requestHandler;
  private final ServiceContext context;
  private final Object parameters;
  private final Method handlerMethod;
  private ContentHandler contentHandler;
  private EntityResolver entityResolver = null;
  private DTDHandler dtdHandler = null;
  private ErrorHandler errorHandler = null;
  private boolean namespaces = true;
  private boolean namespacePrefixes = false;

  private static final String NAMESPACE_PREFIXES_FEATURE = "http://xml.org/sax/features/namespace-prefixes";
  private static final String NAMESPACES_FEATURE = "http://xml.org/sax/features/namespaces";

  public DynamicContentReader(DynamicContentHandler requestHandler, Method handlerMethod,
  ServiceContext context, Object parameters) {
    final String sourceMethod = sourceClass;

    this.requestHandler = requestHandler;
    this.handlerMethod = handlerMethod;
    this.context = context;
    this.parameters = parameters;
    Asserter.assertTrue(sourceClass, sourceMethod, "handlerMethod", handlerMethod != null);

  }

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

  public void parse(String systemId)
  throws IOException, SAXException {
    final String sourceMethod = "parse";

    Asserter.assertTrue(sourceClass,sourceMethod,"contentHandler != null",contentHandler != null);
    contentHandler.startDocument();
    try {
      handleRequest(contentHandler);
    } catch (ServingXmlException e) {
      throw new SAXException(e.getMessage(),e);
    }
    contentHandler.endDocument();
  }

  /**
    * Called by the servingxml framework to obtain XML content as SAX events.
    * @param context The request event
    * @param parameters The parameters
    * @param documentKey Identifier of document
    * @param contentHandler A SAX2 <code>ContentHandler</code> to receive XML content
    * as start/end elements.  The servingxml framework takes care of calling
    * the <code>startDocument</code> and <code>endDocument</code> methods; an
    * implementation should never call these methods itself.
    * This allows an implementation to delegate the work of creating content
    * to other <code>DynamicContent</code> instances.
    *
    */

  private void handleRequest(ContentHandler contentHandler) {

    try {
      ContentWriter contentWriter = new ContentWriter(contentHandler);
      Object[] args = new Object[]{context, parameters, contentWriter};
      handlerMethod.invoke(requestHandler,args);
    } catch (InvocationTargetException e) {
      ServingXmlException sxe = ServingXmlException.fromInvocationTargetException(e);
      throw sxe;
    } catch (java.lang.IllegalAccessException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }
}
