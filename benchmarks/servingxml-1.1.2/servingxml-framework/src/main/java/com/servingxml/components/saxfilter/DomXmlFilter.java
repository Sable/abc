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
import com.servingxml.components.saxfilter.MultipleXmlFilter;
import com.servingxml.components.saxsource.SaxEventBuffer;
import com.servingxml.components.saxsource.SaxEventBufferBuilder;
import com.servingxml.components.saxsource.SaxEventBufferSource;
import com.servingxml.components.string.StringFactory;
import com.servingxml.expr.saxpath.RestrictedMatchPattern;
import com.servingxml.expr.saxpath.SaxPath;
import com.servingxml.io.saxsink.SaxSink;
import com.servingxml.io.saxsink.SimpleSaxSink;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.Stack;
import com.servingxml.util.xml.DefaultSaxErrorHandler;
import com.servingxml.util.xml.XPathBooleanExpression;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import com.servingxml.components.saxsource.DomSaxSourceFactory;
import com.servingxml.util.PrefixMap;

public class DomXmlFilter extends XMLFilterImpl implements XMLFilter, DomWriter {
  private static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();

  private final ServiceContext context; 
  private Flow flow;
  private final PrefixMap prefixMap_;
  private final String base;
  private final XPathBooleanExpression testExpr;
  private final StringFactory messageFactory;
  private LinkedList<StackContext> list;
  private Map<String,String> prefixMap = new HashMap<String,String>();
  private Stack<StackContext> stack;
  private int level = 0;

  public DomXmlFilter(PrefixMap prefixMap, String base, ServiceContext context, Flow flow,
                XPathBooleanExpression testExpr, StringFactory messageFactory) {
    this.prefixMap_ = prefixMap;
    this.base = base;
    this.context = context;
    this.flow = flow;
    this.testExpr = testExpr;
    this.messageFactory = messageFactory;
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
    stack = new Stack<StackContext>();
    stack.push(new StackContext(null,null));
    level = 0;

    super.startDocument();
  }

  public void endDocument() throws SAXException {
    //System.out.println(getClass().getName()+".endDocument enter" );
    if (stack != null && !stack.empty()) {
      stack.pop();
    }
    super.endDocument();
  }

  public void startElement(String namespaceUri, String localName, String qname, Attributes atts)
  throws SAXException {
    //System.out.println(getClass().getName()+".startElement ns=" + namespaceUri + ", name=" + localName + ", qname=" + qname);

    StackContext stackEntry = stack.peek();
    SaxPath saxPathParent = stackEntry.saxPath;

    SaxPath saxPath;
    if (saxPathParent == null) {
      saxPath = new SaxPath(context.getNameTable(),namespaceUri,localName,qname,
                                   atts);
    } else {
      saxPath = new SaxPath(namespaceUri,localName,qname,atts,saxPathParent);
    }
    SaxEventBufferBuilder bufferBuilder = stackEntry.bufferBuilder;
    //System.out.println(getClass().getName()+".startElement matched");
    //if (saxPathParent == null) {
    if (level == 0) {
      bufferBuilder = new SaxEventBufferBuilder();
      bufferBuilder.startDocument();
      Iterator<Map.Entry<String,String>> iter = prefixMap.entrySet().iterator();
      while (iter.hasNext()) {
        Map.Entry<String,String> entry = iter.next();
        bufferBuilder.startPrefixMapping(entry.getValue(),entry.getKey());
      }
    }
    if (bufferBuilder != null) {
      bufferBuilder.startElement(namespaceUri,localName,qname,atts);
    }
    stack.push(new StackContext(saxPath,bufferBuilder));

    super.startElement(namespaceUri,localName,qname,atts);
    ++level;
    //System.out.println(getClass().getName()+".startElement " + qname + " leave");
  }

  public void endElement(String namespaceUri, String localName, String qname)
  throws SAXException {
    --level;
    //System.out.println(getClass().getName()+".endElement enter " + qname);
    try {
      StackContext stackEntry = stack.peek();
      SaxEventBufferBuilder bufferBuilder = stackEntry.bufferBuilder;
      if (bufferBuilder != null) {
        bufferBuilder.endElement(namespaceUri,localName,qname);
        if (level == 0) {
          bufferBuilder.endDocument();
          SaxEventBuffer buffer = bufferBuilder.getBuffer();
          SaxSource saxSource = new SaxEventBufferSource(buffer,"", context.getTransformerFactory());

          DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
          builderFactory.setValidating(false);
          builderFactory.setNamespaceAware(true);
          DocumentBuilder builder = builderFactory.newDocumentBuilder();

          XMLReader reader = saxSource.createXmlReader();
          //InputSource inputSource = new InputSource(reader);
          //Document doc = builder.parse(inputSource);

        }
      }
      stack.pop();
    } catch (ServingXmlException e) {
      throw new SAXException(e.getMessage(), e);
    } catch (Exception e) {
      throw new SAXException(e.getMessage(), e);
    }

    super.endElement(namespaceUri,localName,qname);
    //System.out.println(getClass().getName()+".endElement " + qname + " leave");
  }

  public void setDocumentLocator(Locator locator) {
    if (stack != null && !stack.empty()) {
      StackContext stackEntry = stack.peek();
      SaxEventBufferBuilder bufferBuilder = stackEntry.bufferBuilder;
      if (bufferBuilder != null) {
        bufferBuilder.setDocumentLocator(locator);
      }
    }
    super.setDocumentLocator(locator);
  }

  public void startPrefixMapping (String prefix, String uri)
  throws SAXException {
    //System.out.println(getClass()+".startPrefixMapping " + prefix+":" + uri);

    StackContext stackEntry = stack.peek();
    SaxEventBufferBuilder bufferBuilder = stackEntry.bufferBuilder;
    if (bufferBuilder != null) {
      bufferBuilder.startPrefixMapping(prefix,uri);
    } else {
      prefixMap.put(uri,prefix);
    }

    super.startPrefixMapping(prefix,uri);
  }

  public void endPrefixMapping (String prefix)
  throws SAXException {

    StackContext stackEntry = stack.peek();
    SaxEventBufferBuilder bufferBuilder = stackEntry.bufferBuilder;
    if (bufferBuilder != null) {
      bufferBuilder.endPrefixMapping(prefix);
    }

    super.endPrefixMapping(prefix);
  }

  public void ignorableWhitespace (char ch[], int start, int length)
  throws SAXException {
    StackContext stackEntry = stack.peek();
    SaxEventBufferBuilder bufferBuilder = stackEntry.bufferBuilder;
    if (bufferBuilder != null) {
      bufferBuilder.ignorableWhitespace(ch,start,length);
    }
    super.ignorableWhitespace(ch,start,length);
  }

  public void processingInstruction (String target, String data)
  throws SAXException {
    StackContext stackEntry = stack.peek();
    SaxEventBufferBuilder bufferBuilder = stackEntry.bufferBuilder;
    if (bufferBuilder != null) {
      bufferBuilder.processingInstruction(target,data);
    }
    super.processingInstruction(target,data);
  }

  public void skippedEntity (String name)
  throws SAXException {
    StackContext stackEntry = stack.peek();
    SaxEventBufferBuilder bufferBuilder = stackEntry.bufferBuilder;
    if (bufferBuilder != null) {
      bufferBuilder.skippedEntity(name);
    }
    super.skippedEntity(name);
  }

  public void characters(char[] ch, int start, int length) 
  throws SAXException {
    StackContext stackEntry = stack.peek();
    SaxEventBufferBuilder bufferBuilder = stackEntry.bufferBuilder;
    if (bufferBuilder != null) {
      bufferBuilder.characters(ch,start,length);
    }
    super.characters(ch,start,length);
  }

  public void setErrorHandler(ErrorHandler errorHandler) {
    // No op
  }

  static class StackContext {
    final SaxPath saxPath;
    final SaxEventBufferBuilder bufferBuilder;

    StackContext(SaxPath saxPath, SaxEventBufferBuilder bufferBuilder) {
      this.saxPath = saxPath;
      this.bufferBuilder = bufferBuilder;
    }
  }

  public void write(ServiceContext context, Flow flow, Document document) {
  }
}


