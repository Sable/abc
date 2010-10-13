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

package com.servingxml.components.xslt;

import java.io.IOException;

import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.parameter.WithParameters;
import com.servingxml.util.Name;
import com.servingxml.util.record.Record;
import com.servingxml.util.xml.ContentHandlerFilter;
import com.servingxml.util.xml.UriResolverFactory;
import com.servingxml.app.Environment;

/**
 *
 *  01/04/03
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class XsltFilter implements XMLFilter {

  private final Environment env;
  private final Templates templates;                           
  private final ServiceContext context;
  private final Flow flow;
  private final String documentBase;
  private final WithParameters withParameters;

  private XMLReader parent = null;
  private ContentHandler handler = null;
  private EntityResolver entityResolver = null;
  private DTDHandler dtdHandler = null;
  private ErrorHandler errorHandler = null;

  public XsltFilter(Environment env, ServiceContext context, Flow flow,
  Templates templates, String documentBase, WithParameters withParameters) {
    this.env = env;
    this.context = context;
    this.flow = flow;
    this.templates = templates;
    this.documentBase = documentBase;
    this.withParameters = withParameters;
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
    return handler;
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
    this.handler = handler;
  }

  public ErrorHandler getErrorHandler() {
    return errorHandler;
  }

  public void parse(InputSource inputSource) throws IOException,SAXException {
    setupParse();
    if (parent != null) {
      parent.parse(inputSource);
    }
  }

  public void parse(String systemId) throws IOException,SAXException {
    setupParse();
    if (parent != null) {
      parent.parse(systemId);
    }
  }

  private void setupParse() throws IOException,SAXException {
    try {
      //long start = Runtime.getRuntime().totalMemory();
      //System.out.println(getClass().getName() + ".setupParse enter mem=" + start);
      Record parameters = flow.getParameters();
      SAXTransformerFactory saxFactory = context.getTransformerFactory();
      //  TODO:  init
      TransformerHandler transformerHandler = saxFactory.newTransformerHandler(templates);


      if (handler != null) {
        Result result = new SAXResult(handler);
        transformerHandler.setResult(result);
      }
      final Transformer transformer = transformerHandler.getTransformer();

      UriResolverFactory resolverFactory = context.getUriResolverFactory();
      URIResolver uriResolver = resolverFactory.createUriResolver(env.getQnameContext().getPrefixMap(),
                                                                  documentBase,
                                                                  parameters,
                                                                  transformer.getURIResolver());
      transformer.setURIResolver(uriResolver);  

      //System.out.println(getClass().getName()+".setupParse "+parameters.toXmlString(context));
      for (int i = 0; i < parameters.fieldCount(); ++i) {
        Name parameterName = parameters.getFieldName(i);
        if (withParameters.accept(parameterName)) {
          String value = parameters.getString(parameterName);
          if (value != null) {
            transformer.setParameter(parameterName.toString(),value);
          }
        }
      }
      if (parent != null) {
        //DEBUG
        /* ContentHandler handler = new ContentHandlerFilter(transformerHandler) {
            public void startDocument() throws SAXException {
              //System.out.println(getClass().getName()+".startDocument");
              super.startDocument();
            }
            public void endDocument() throws SAXException {
              //System.out.println(getClass().getName()+".endDocument");
              super.endDocument();
            }
        };*/

        parent.setContentHandler(transformerHandler);
      }
      //long end = Runtime.getRuntime().totalMemory();
      //System.out.println(getClass().getName() + ".setupParse leave mem=" + end);
    } catch (TransformerException te) {
      throw new SAXException(te.getMessage(),te);
    }
  }
}

