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

package com.servingxml.components.choose;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import org.xml.sax.ErrorHandler;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.ContentHandler;

import com.servingxml.app.ServiceContext;
import com.servingxml.components.saxsource.SaxEventBuffer;
import com.servingxml.components.saxsource.SaxEventBufferBuilder;
import com.servingxml.components.saxsource.SaxEventBufferSource;
import com.servingxml.expr.saxpath.SaxPath;
import com.servingxml.app.Flow;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.Stack;
import com.servingxml.io.saxsink.SaxSink;
import com.servingxml.io.saxsink.SimpleSaxSink;
import com.servingxml.util.xml.XsltChooser;
import com.servingxml.app.xmlpipeline.XmlPipeline;
import com.servingxml.components.saxfilter.MultipleXmlFilter;
import com.servingxml.components.content.Content;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;
import com.servingxml.util.xml.DefaultSaxErrorHandler;

public class ChooseXmlFilter extends XMLFilterImpl implements XMLFilter {
  private static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();

  private final Alternative[] alternatives;
  private final XsltChooser chooser;
  private final ServiceContext context; 
  private Flow flow;
  private Stack<StackContext> stack;
  private Map<String,String> prefixMap = new HashMap<String,String>();
  private final XMLFilter[] xmlFilters;

  public ChooseXmlFilter(ServiceContext context, Flow flow,
                         Alternative[] alternatives, XsltChooser chooser) {
    this.context = context;
    this.flow = flow;
    this.alternatives = alternatives;
    this.chooser = chooser;
    this.xmlFilters = new XMLFilter[alternatives.length];

    for (int i = 0; i < alternatives.length; ++i) {
      Content[] xmlComponents = alternatives[i].getXmlComponents();
      if (xmlComponents.length > 0) {
        MultipleXmlFilter multipleXmlFilter = new MultipleXmlFilter();
        for (int j = 0; j < xmlComponents.length; ++j) {
          Content xmlFilterAppender = xmlComponents[j];
          xmlFilterAppender.appendToXmlPipeline(context, flow, multipleXmlFilter);
        }
        xmlFilters[i] = multipleXmlFilter;
      } else {
        xmlFilters[i] = null;
      }
    }
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
    stack.push(new StackContext(null,null,false));

    //super.startDocument();
  }

  public void endDocument() throws SAXException {
    //System.out.println(getClass().getName()+".endDocument enter" );
    if (stack != null && !stack.empty()) {
      stack.pop();
    }
    //super.endDocument();
  }

  public void startElement(String namespaceUri, String localName, String qname, Attributes atts)
  throws SAXException {
    //System.out.println(getClass().getName()+".startElement ns=" + namespaceUri + ", name=" + localName + ", qname=" + qname);

    StackContext stackEntry = stack.peek();
    SaxPath parent = stackEntry.saxPath;

    SaxPath saxPath;
    if (parent == null) {
      saxPath = new SaxPath(context.getNameTable(),namespaceUri,localName,qname,
                                   atts);
    } else {
      saxPath = new SaxPath(namespaceUri,localName,qname,atts,parent);
    }
    SaxEventBufferBuilder bufferBuilder = stackEntry.bufferBuilder;
    boolean isDocument = false;
    //System.out.println(getClass().getName()+".startElement matched");
    if (parent == null) {
      bufferBuilder = new SaxEventBufferBuilder();
      bufferBuilder.startDocument();
      isDocument = true;
      Iterator<Map.Entry<String,String>> iter = prefixMap.entrySet().iterator();
      while (iter.hasNext()) {
        Map.Entry<String,String> entry = iter.next();
        bufferBuilder.startPrefixMapping(entry.getValue(),entry.getKey());
      }
    }
    if (bufferBuilder != null) {
      bufferBuilder.startElement(namespaceUri,localName,qname,atts);
    }
    stack.push(new StackContext(saxPath,bufferBuilder,isDocument));

    //super.startElement(namespaceUri,localName,qname,atts);
    //System.out.println(getClass().getName()+".endElement " + qname + " leave");
  }

  public void endElement(String namespaceUri, String localName, String qname)
  throws SAXException {
    //System.out.println(getClass().getName()+".endElement enter " + qname);
    try {
      StackContext stackEntry = stack.peek();
      SaxEventBufferBuilder bufferBuilder = stackEntry.bufferBuilder;
      if (bufferBuilder != null) {
        bufferBuilder.endElement(namespaceUri,localName,qname);
        if (stackEntry.isDocument) {
          bufferBuilder.endDocument();
          SaxEventBuffer buffer = bufferBuilder.getBuffer();
          SaxSource saxSource = new SaxEventBufferSource(buffer,"", context.getTransformerFactory());

          //System.out.println(getClass().getName()+".endElement "+buffer.toString());
          XMLReader reader = saxSource.createXmlReader();
          Source xmlSource = new SAXSource(reader,new InputSource(""));
          int index = chooser.choose(xmlSource, flow.getParameters());
          if (index >= 0 && index < alternatives.length) {
            Flow newFlow = flow.replaceDefaultSaxSource(context, saxSource);
            XMLFilter filter = xmlFilters[index];
            if (filter != null) {
              XmlPipeline pipeline = new XmlPipeline();
              pipeline.setSaxSource(saxSource);
              pipeline.addXmlFilter(filter);
              pipeline.execute(getContentHandler());
            }
          }
        }
      }
      stack.pop();
    } catch (ServingXmlException e) {
      throw new SAXException(e.getMessage(), e);
    }

    //super.endElement(namespaceUri,localName,qname);
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
    //super.setDocumentLocator(locator);
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

    //super.startPrefixMapping(prefix,uri);
  }

  public void endPrefixMapping (String prefix)
  throws SAXException {

    StackContext stackEntry = stack.peek();
    SaxEventBufferBuilder bufferBuilder = stackEntry.bufferBuilder;
    if (bufferBuilder != null) {
      bufferBuilder.endPrefixMapping(prefix);
    }

    //super.endPrefixMapping(prefix);
  }

  public void ignorableWhitespace (char ch[], int start, int length)
  throws SAXException {
    StackContext stackEntry = stack.peek();
    SaxEventBufferBuilder bufferBuilder = stackEntry.bufferBuilder;
    if (bufferBuilder != null) {
      bufferBuilder.ignorableWhitespace(ch,start,length);
    }
    //super.ignorableWhitespace(ch,start,length);
  }

  public void processingInstruction (String target, String data)
  throws SAXException {
    StackContext stackEntry = stack.peek();
    SaxEventBufferBuilder bufferBuilder = stackEntry.bufferBuilder;
    if (bufferBuilder != null) {
      bufferBuilder.processingInstruction(target,data);
    }
    //super.processingInstruction(target,data);
  }

  public void skippedEntity (String name)
  throws SAXException {
    StackContext stackEntry = stack.peek();
    SaxEventBufferBuilder bufferBuilder = stackEntry.bufferBuilder;
    if (bufferBuilder != null) {
      bufferBuilder.skippedEntity(name);
    }
    //super.skippedEntity(name);
  }

  public void characters(char[] ch, int start, int length) 
  throws SAXException {
    StackContext stackEntry = stack.peek();
    SaxEventBufferBuilder bufferBuilder = stackEntry.bufferBuilder;
    if (bufferBuilder != null) {
      bufferBuilder.characters(ch,start,length);
    }
    //super.characters(ch,start,length);
  }

  public void setErrorHandler(ErrorHandler errorHandler) {
    // No op
  }

  static class StackContext {
    final SaxPath saxPath;
    final SaxEventBufferBuilder bufferBuilder;
    final boolean isDocument;

    StackContext(SaxPath saxPath, SaxEventBufferBuilder bufferBuilder, boolean isDocument) {
      this.saxPath = saxPath;
      this.bufferBuilder = bufferBuilder;
      this.isDocument = isDocument;
    }
  }
}


