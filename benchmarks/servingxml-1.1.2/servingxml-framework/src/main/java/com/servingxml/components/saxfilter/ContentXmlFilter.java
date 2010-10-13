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

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.xmlpipeline.XmlPipeline;
import com.servingxml.io.saxsink.SaxSink;
import com.servingxml.io.saxsink.SimpleSaxSink;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.xml.DefaultSaxErrorHandler;
import java.io.IOException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

public class ContentXmlFilter extends XMLFilterImpl implements XMLFilter {
  private static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();

  private final ServiceContext context; 
  private Flow flow;
  private final SaxSource saxSource;
  private int level = 0;

  public ContentXmlFilter(ServiceContext context, Flow flow, SaxSource saxSource) {
    this.context = context;
    this.flow = flow;
    this.saxSource = saxSource;
    this.level = 0;
    setErrorHandler(new DefaultSaxErrorHandler(context));
  }

  public void setParent(XMLReader parent) {
    super.setParent(parent);
    //if (parent != null) {
      //System.out.println(getClass().getName()+".setParent " + parent.getClass().getName());
    //} else {
      //System.out.println(getClass().getName()+".setParent to null");
    //}
  }

  public void setContentHandler(ContentHandler handler) {
    //System.out.println(getClass().getName()+".setContentHandler " + handler.getClass().getName());
    SaxSink saxSink = new SimpleSaxSink(handler);
    flow = flow.replaceDefaultSaxSink(context, saxSink);
    super.setContentHandler(handler);
  }

  public void startDocument() throws SAXException {
    //System.out.println(getClass().getName()+".startDocument enter" );
    level = 0;

    //super.startDocument();
  }

  public void endDocument() throws SAXException {
    //System.out.println(getClass().getName()+".endDocument enter" );
    level = 0;
    //super.endDocument();
  }

  public void startElement(String namespaceUri, String localName, String qname, Attributes atts)
  throws SAXException {
    //System.out.println(getClass().getName()+".appendToPipeline startElement " + qname + " " + level);
    ++level;
  }

  public void endElement(String namespaceUri, String localName, String qname)
  throws SAXException {
    //System.out.println(getClass().getName()+".appendToPipeline endElement " + qname + " " + level);
    --level;
    //if (level == 0) {
    //  XmlPipeline pipeline = new XmlPipeline();
    //  pipeline.setSaxSource(saxSource);
    //  pipeline.execute(getContentHandler());
    //}
  }

  public void setDocumentLocator(Locator locator) {
  }

  public void startPrefixMapping (String prefix, String uri)
  throws SAXException {
  }

  public void endPrefixMapping (String prefix)
  throws SAXException {
  }

  public void ignorableWhitespace (char ch[], int start, int length)
  throws SAXException {
  }

  public void processingInstruction (String target, String data)
  throws SAXException {
  }

  public void skippedEntity (String name)
  throws SAXException {
  }

  public void characters(char[] ch, int start, int length) 
  throws SAXException {
  }

  public void setErrorHandler(ErrorHandler errorHandler) {
    // No op
  }

  public void parse(InputSource input)
  throws SAXException, IOException {
    //System.out.println(getClass().getName()+".parse");
    super.parse(input);
    XMLReader reader = saxSource.createXmlReader();
    //System.out.println(getClass().getName()+".parse contentHandler " + getContentHandler().getClass().getName());
    reader.setContentHandler(getContentHandler());
    reader.parse(input);
  }

  public void parse(String systemId)
  throws SAXException, IOException {
    parse(new InputSource(systemId));
  }
}


