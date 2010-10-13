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
import com.servingxml.components.saxsource.SaxEventBuffer;
import com.servingxml.components.saxsource.SaxEventBufferBuilder;
import com.servingxml.components.saxsource.SaxEventBufferSource;
import com.servingxml.components.task.Task;
import com.servingxml.expr.saxpath.RestrictedMatchPattern;
import com.servingxml.expr.saxpath.SaxPath;
import com.servingxml.io.saxsink.SaxSink;
import com.servingxml.io.saxsink.SimpleSaxSink;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.Stack;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;
import com.servingxml.app.Environment;

public class ProcessSubtree extends XMLFilterImpl implements XMLFilter {
  private static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();

  private final Environment env;
  private final ServiceContext context; 
  private Flow flow;
  private final RestrictedMatchPattern expr;
  private final Task[] tasks;
  private Stack<StackContext> stack;
  private Map<String,String> prefixMap = new HashMap<String,String>();

  public ProcessSubtree(Environment env, ServiceContext context, Flow flow,
  RestrictedMatchPattern expr, Task[] tasks) {
    this.env = env;
    this.context = context;
    this.flow = flow;
    this.expr = expr;
    this.tasks = tasks;                               

    //System.out.println(flow.getRecord().toXmlString(context));
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
    if (stack != null && !stack.empty()) {
      stack.pop();
    }
    //super.endDocument();
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
    boolean isDocument = false;
    if (expr.match(saxPath,flow.getParameters())) {
      //System.out.println(getClass().getName()+".startElement matched");
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

          Flow newFlow = env.augmentParametersOf(context, flow.replaceDefaultSaxSource(context, saxSource));
          for (int i = 0; i < tasks.length; ++i) {
            Task task = tasks[i];     
            task.execute(context, newFlow);
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


