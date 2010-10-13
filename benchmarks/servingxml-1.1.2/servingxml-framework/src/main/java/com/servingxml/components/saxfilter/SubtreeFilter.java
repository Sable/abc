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
import com.servingxml.app.xmlpipeline.XmlFilterChain;
import com.servingxml.components.task.Task;
import com.servingxml.expr.saxpath.RestrictedMatchPattern;
import com.servingxml.expr.saxpath.SaxPath;
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.ExpirableFamily;
import com.servingxml.io.saxsink.SaxSink;
import com.servingxml.io.saxsink.SimpleSaxSink;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.Stack;
import com.servingxml.util.xml.FragmentContentHandler;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

public class SubtreeFilter extends XMLFilterImpl implements XMLFilter, XmlFilterChain {
  private static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();

  private final ServiceContext context; 
  private Flow flow;
  private final RestrictedMatchPattern expr;
  private Stack<SaxPath> stack;
  private Map<String,String> prefixMap = new HashMap<String,String>();
  private XMLFilterImpl headFilter;
  private XMLFilter tailFilter;
  private int level = 0;
  private int subtreeLevel = -1;
  private Properties outputProperties = new Properties();

  public SubtreeFilter(ServiceContext context, Flow flow, RestrictedMatchPattern expr) {
    this.context = context;
    this.flow = flow;
    this.expr = expr;
    this.headFilter = new MyFilter();
    this.tailFilter = headFilter;
    stack = new Stack<SaxPath>();
    level = 0;

    //System.out.println(flow.getRecord().toXmlString(context));
  }

  public void setParent(XMLReader parent) {
    super.setParent(parent);
  }

  public void setContentHandler(ContentHandler handler) {
/*
    handler = new XMLFilterImpl() {
      public void startElement(String namespaceUri, String localName, String qname, Attributes atts) {
        //System.out.println("<"+qname+">");
      }
      public void endElement(String namespaceUri, String localName, String qname) {
        //System.out.println("</"+qname+">");
      }

      public void characters(char[] ch, int start, int length) 
      throws SAXException {
        String s = new String(ch,start,length);
        //System.out.println(s);
      }
    };
*/
    tailFilter.setContentHandler(new FragmentContentHandler(handler));
    super.setContentHandler(handler);
  }

  public void startDocument() throws SAXException {
    //System.out.println(getClass().getName()+".startDocument enter" );
    super.startDocument();
    try {
      tailFilter.parse("");
    } catch (IOException e) {
    }
  }

  public void endDocument() throws SAXException {
    super.endDocument();
  }

  public void startElement(String namespaceUri, String localName, String qname, Attributes atts)
  throws SAXException {
    //System.out.println(getClass().getName()+".startElement ns=" + namespaceUri + ", name=" + localName + ", qname=" + qname);

    SaxPath saxPath;
    if (stack.empty()) {
      saxPath = new SaxPath(context.getNameTable(),namespaceUri,localName,qname,atts);
    } else {
      SaxPath saxPathParent = stack.peek();
      saxPath = new SaxPath(namespaceUri,localName,qname,atts,saxPathParent);
    }
    stack.push(saxPath);

    if (subtreeLevel < 0 && expr.match(saxPath,flow.getParameters())) {
      subtreeLevel = level;
      headFilter.startDocument();
    } 

    //System.out.println(getClass().getName()+".startElement qname="+qname+", level="+level + ", subtreeLevel="+subtreeLevel);
    if (subtreeLevel >= 0) {
      headFilter.startElement(namespaceUri,localName,qname,atts);
    } else {
      super.startElement(namespaceUri,localName,qname,atts);
    }
    ++level;
    //System.out.println(getClass().getName()+".endElement " + qname + " leave");
  }

  public void endElement(String namespaceUri, String localName, String qname)
  throws SAXException {
    --level;
    stack.pop();
    //System.out.println(getClass().getName()+".startElement qname="+qname+", level="+level + ", subtreeLevel="+subtreeLevel);
    if (subtreeLevel >= 0) {
      headFilter.endElement(namespaceUri,localName,qname);
    } else {
      super.endElement(namespaceUri,localName,qname);
    }
    if (subtreeLevel == level) {
      subtreeLevel = -1;
      headFilter.endDocument();
    }
    //System.out.println(getClass().getName()+".endElement " + qname + " leave");
  }

  public void setDocumentLocator(Locator locator) {
    super.setDocumentLocator(locator);
  }

  public void startPrefixMapping (String prefix, String uri)
  throws SAXException {
    //System.out.println(getClass()+".startPrefixMapping " + prefix+":" + uri);

    headFilter.startPrefixMapping(prefix,uri);
    super.startPrefixMapping(prefix,uri);
  }

  public void endPrefixMapping (String prefix)
  throws SAXException {

    headFilter.endPrefixMapping(prefix);
    super.endPrefixMapping(prefix);
  }

  public void ignorableWhitespace (char ch[], int start, int length)
  throws SAXException {
    if (subtreeLevel >= 0) {
      headFilter.ignorableWhitespace(ch,start,length);
    } else {
      super.ignorableWhitespace(ch,start,length);
    }
  }

  public void processingInstruction (String target, String data)
  throws SAXException {
    if (subtreeLevel >= 0) {
      headFilter.processingInstruction(target,data);
    } else {
      super.processingInstruction(target,data);
    }
  }

  public void skippedEntity (String name)
  throws SAXException {
    if (subtreeLevel >= 0) {
      headFilter.skippedEntity(name);
    } else {
      super.skippedEntity(name);
    }
  }

  public void characters(char[] ch, int start, int length) 
  throws SAXException {
    if (subtreeLevel >= 0) {
      headFilter.characters(ch,start,length);
    } else {
      super.characters(ch,start,length);
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


