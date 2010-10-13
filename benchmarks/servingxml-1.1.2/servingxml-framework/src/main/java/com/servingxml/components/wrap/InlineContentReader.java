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

package com.servingxml.components.wrap;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.common.NameSubstitutionExpr;
import com.servingxml.components.string.Stringable;
import com.servingxml.util.Name;
import com.servingxml.util.PrefixMap;
import com.servingxml.util.QnameContext;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;
import com.servingxml.util.xml.DefaultSaxErrorHandler;
import java.io.IOException;
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
import org.xml.sax.helpers.AttributesImpl;

public class InlineContentReader
  implements XMLReader, EntityResolver, DTDHandler {

  private final QnameContext nameContext;
  private Locator locator = null;
  private EntityResolver entityResolver = null;
  private DTDHandler dtdHandler = null;
  private ContentHandler contentHandler = null;
  private ErrorHandler errorHandler = null;
  private final ServiceContext context;
  private final Flow flow;
  private final NameSubstitutionExpr nameResolver;
  private final AttributesImpl attributes;
  private final Stringable stringFactory;
  private String literalNamespaceUri = "";
  private String literalLocalName = "";
  private String literalQname = "";
  private final XMLReader[] children;

  public InlineContentReader(QnameContext nameContext, ServiceContext context, Flow flow, 
    NameSubstitutionExpr nameResolver, AttributesImpl attributes, 
    Stringable stringFactory, XMLReader[] children) {

    this.nameContext = nameContext;
    this.context = context;
    this.flow = flow;
    this.nameResolver = nameResolver;
    this.attributes = attributes;
    this.stringFactory = stringFactory;
    this.children = children;
    this.errorHandler = new DefaultSaxErrorHandler(context);
  }

  public void setFeature(String name, boolean value)
  throws SAXNotRecognizedException, SAXNotSupportedException {
  }

  public boolean getFeature(String name)
  throws SAXNotRecognizedException, SAXNotSupportedException {
    throw new SAXNotRecognizedException("Feature: " + name);
  }

  public void setProperty(String name, Object value)
  throws SAXNotRecognizedException, SAXNotSupportedException {
  }

  public Object getProperty(String name)
  throws SAXNotRecognizedException, SAXNotSupportedException {
    throw new SAXNotRecognizedException("Property: " + name);
  }

  public void setEntityResolver(EntityResolver resolver) {
    entityResolver = resolver;
  }

  public EntityResolver getEntityResolver () {
    return entityResolver;
  }

  public void setDTDHandler(DTDHandler handler) {
    dtdHandler = handler;
  }

  public DTDHandler getDTDHandler() {
    return dtdHandler;
  }

  public void setContentHandler(ContentHandler handler) {
    this.contentHandler = handler;
    //System.out.println(getClass().getName()+".setContentHandler " + handler.getClass().getName());
    //for (int i = 0; i < children.length; ++i) {
      //children[i].setContentHandler(handler);
    //}
  }

  public ContentHandler getContentHandler() {
    return contentHandler;
  }

  public void setErrorHandler(ErrorHandler handler) {
    // No op;
  }

  public ErrorHandler getErrorHandler() {
    return errorHandler;
  }

  public void parse(InputSource input)
  throws SAXException, IOException {
    //System.out.println(getClass().getName()+".parse enter contentHandler " + contentHandler.getClass().getName());
    //if (contentHandler == null) {
      //System.out.println(getClass().getName()+".parse content handler is null");
    //}

    contentHandler.startDocument();
    startLiteralContent();
    for (int i = 0; i < children.length; ++i) {
      children[i].setContentHandler(contentHandler);
      children[i].parse(input);
    }
    endLiteralContent();
    contentHandler.endDocument();
  }

  public void parse(String systemId)
  throws SAXException, IOException {
    parse(new InputSource(systemId));
  }

  public InputSource resolveEntity(String publicId, String systemId)
  throws SAXException, IOException {
    if (entityResolver != null) {
      return entityResolver.resolveEntity(publicId, systemId);
    } else {
      return null;
    }
  }

  public void notationDecl(String name, String publicId, String systemId)
  throws SAXException {
    if (dtdHandler != null) {
      dtdHandler.notationDecl(name, publicId, systemId);
    }
  }

  public void unparsedEntityDecl(String name, String publicId,
    String systemId, String notationName) throws SAXException
  {
    if (dtdHandler != null) {
      dtdHandler.unparsedEntityDecl(name, publicId, systemId,
        notationName);
    }
  }

  protected void startLiteralContent() throws SAXException {
    //System.out.println(getClass().getName()+".startLiteralContent enter");
    Record parameters = flow.getParameters();
    Record record = flow.getRecord();

    Name elementName = nameResolver.evaluateName(parameters,record);
    this.literalNamespaceUri = elementName.getNamespaceUri();
    this.literalLocalName = elementName.getLocalName();
    this.literalQname = elementName.toQname(nameContext);

    //System.out.println(getClass().getName()+".startLiteralContent element="+literalQname);

    PrefixMap.PrefixMapping[] prefixDeclarations = nameContext.getPrefixMap().getLocalPrefixDeclarations();
    for (int i = 0; i < prefixDeclarations.length; ++i) {
      PrefixMap.PrefixMapping prefixMapping = prefixDeclarations[i];
      //System.out.println(getClass().getName()+".generateElement prefix="+prefixMapping.getPrefix() + ", ns = " + prefixMapping.getNamespaceUri());
      contentHandler.startPrefixMapping(prefixMapping.getPrefix(), prefixMapping.getNamespaceUri());
    }
            
    AttributesImpl literalAttributes = new AttributesImpl();
    for (int i = 0; i < attributes.getLength();++i) {
      literalAttributes.addAttribute(attributes.getURI(i),attributes.getLocalName(i),attributes.getQName(i),
        attributes.getType(i),attributes.getValue(i));
      //System.out.println("InlineContentReader attribute {" + attributes.getURI(i) 
      //                   + "}" + attributes.getLocalName(i) + " "
      //                   + attributes.getQName(i));
    }

    //System.out.println(getClass().getName()+".startLiteralContent before startElement " + contentHandler.getClass().getName());
    contentHandler.startElement(literalNamespaceUri,literalLocalName,literalQname,literalAttributes);
    //System.out.println(getClass().getName()+".startLiteralContent after startElement");
    String value = stringFactory.createString(context,flow);
    contentHandler.characters(value.toCharArray(),0,value.length());
  }

  protected void endLiteralContent() throws SAXException {
    //ContentHandler contentHandler = flow.getDefaultSaxSink().getContentHandler();

    contentHandler.endElement(literalNamespaceUri,literalLocalName,literalQname);
  }

}


