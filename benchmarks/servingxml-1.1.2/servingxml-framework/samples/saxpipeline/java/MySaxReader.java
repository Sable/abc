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

import java.io.IOException;

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
 * A <code>HTTPParameterReader</code> implement a SAX 2 <code>XMLReader</code> interface for supplying 
 * HTTP parameter values as SAX events.
 *
 * 
 * @author  Daniel A. Parker
 */

public class MySaxReader implements XMLReader {

  private static final String NAMESPACE_PREFIXES_FEATURE = "http://xml.org/sax/features/namespace-prefixes";
  private static final String NAMESPACES_FEATURE = "http://xml.org/sax/features/namespaces";
  private static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();

  private ContentHandler contentHandler;
  private EntityResolver entityResolver = null;
  private DTDHandler dtdHandler = null;
  private ErrorHandler errorHandler = null;
  private boolean namespaces = true;
  private boolean namespacePrefixes = false;

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
    if (contentHandler != null) {
      contentHandler.startDocument();
      contentHandler.startElement("","HTML","HTML",EMPTY_ATTRIBUTES);
      contentHandler.startElement("","Head","Head",EMPTY_ATTRIBUTES);
      contentHandler.startElement("","TITLE","TITLE",EMPTY_ATTRIBUTES);
      char[] title = "This is a mixed-case HTML-like XML document".toCharArray();
      contentHandler.characters(title,0,title.length);
      contentHandler.endElement("","TITLE","TITLE");
      contentHandler.endElement("","Head","Head");
      contentHandler.startElement("","Body","Body",EMPTY_ATTRIBUTES);
      contentHandler.startElement("","p","p",EMPTY_ATTRIBUTES);
      char[] s1 = "some ".toCharArray();
      contentHandler.characters(s1,0,s1.length);
      contentHandler.startElement("","I","I",EMPTY_ATTRIBUTES);
      char[] s2 = "HTML".toCharArray();
      contentHandler.characters(s2,0,s2.length);
      contentHandler.endElement("","I","I");
      char[] s3 = " paragraph".toCharArray();
      contentHandler.characters(s3,0,s3.length);
      contentHandler.endElement("","p","p");
      contentHandler.startElement("","data","data",EMPTY_ATTRIBUTES);
      contentHandler.startElement("","record","record",EMPTY_ATTRIBUTES);
      char[] value1 = "one".toCharArray();
      contentHandler.characters(value1,0,value1.length);
      contentHandler.endElement("","record","record");
      contentHandler.startElement("","record","record",EMPTY_ATTRIBUTES);
      char[] value2 = "two".toCharArray();
      contentHandler.characters(value2,0,value2.length);
      contentHandler.endElement("","record","record");
      contentHandler.endElement("","data","data");
      contentHandler.startElement("","UL","UL",EMPTY_ATTRIBUTES);
      contentHandler.startElement("","LI","LI",EMPTY_ATTRIBUTES);
      char[] alpha = "alpha".toCharArray();
      contentHandler.characters(alpha,0,alpha.length);
      contentHandler.endElement("","LI","LI");
      contentHandler.startElement("","LI","LI",EMPTY_ATTRIBUTES);
      char[] beta = "beta".toCharArray();
      contentHandler.characters(beta,0,beta.length);
      contentHandler.endElement("","LI","LI");
      contentHandler.startElement("","LI","LI",EMPTY_ATTRIBUTES);
      char[] gamma = "gamma".toCharArray();
      contentHandler.characters(gamma,0,gamma.length);
      contentHandler.endElement("","LI","LI");
      contentHandler.endElement("","UL","UL");
      contentHandler.endElement("","Body","Body");
      contentHandler.endElement("","HTML","HTML");
      contentHandler.endDocument();
    }
  }
}
