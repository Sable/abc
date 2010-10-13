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

package com.servingxml.components.serializer;

import java.util.Properties;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.saxsink.SaxSinkFactory;
import com.servingxml.expr.saxpath.RestrictedMatchPattern;
import com.servingxml.expr.saxpath.SaxPath;
import com.servingxml.io.saxsink.SaxSink;
import com.servingxml.util.Name;
import com.servingxml.util.PrefixMap;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.Stack;
import com.servingxml.util.SystemConstants;
import com.servingxml.util.record.ParameterBuilder;
import com.servingxml.util.record.Record;
import com.servingxml.util.xml.ContentHandlerFilter;
import com.servingxml.components.saxsource.SaxEventBufferBuilder;
import com.servingxml.components.saxsource.SaxEventBuffer;

public class BatchedContentHandler extends ContentHandlerFilter {
  private static final Name BATCH_SEQUENCE_NUMBER_NAME= new QualifiedName(SystemConstants.SERVINGXML_NS_URI,"batchSequenceNumber");

  private final ServiceContext context; 
  private Flow flow;
  private final RestrictedMatchPattern expr;
  private final long batchSize;
  private final int maxFiles;
  private final SaxSinkFactory saxSinkFactory;
  private Properties outputProperties = new Properties();
  private Stack<SaxPath> stack = new Stack<SaxPath>();
  private int level = 0;
  private long batchSequenceNumber = 0;
  private long subtreeCount = 0;
  private SaxSink saxSink;
  private SaxEventBufferBuilder bufferBuilder = new SaxEventBufferBuilder();
  private SaxEventBuffer eventBuffer;
  private boolean started = false;

  public BatchedContentHandler(ServiceContext context, Flow flow, 
                               SaxSinkFactory saxSinkFactory,
                               RestrictedMatchPattern expr, long batchSize, int maxFiles) {
    this.context = context;
    this.flow = flow;
    this.saxSinkFactory = saxSinkFactory;
    this.expr = expr;
    this.batchSize = batchSize;
    this.maxFiles = maxFiles;
  }

  public void setOutputProperties(Properties outputProperties) {
    this.outputProperties = outputProperties;
  }

  public void startDocument() throws SAXException {
    bufferBuilder.startDocument();
  }

  public void endDocument() throws SAXException {
    if (subtreeCount > 0) {
      endBatch();
    }
  }

  public void startElement(String namespaceUri, String localName, String qname, Attributes atts)
  throws SAXException {
    SaxPath parent = stack.empty() ? null : stack.peek();
    SaxPath contextElement;
    if (parent == null) {
      contextElement = new SaxPath(context.getNameTable(), namespaceUri, localName, qname, atts);
    } else {
      contextElement = new SaxPath(namespaceUri, localName, qname, atts, parent);
    }
    boolean matched;
    if (level > 0) {
      matched = true;
    } else if (expr.match(contextElement, flow.getParameters())) {
      if (!started) {
        eventBuffer = bufferBuilder.getBuffer();
        started = true;
      }
      matched = true;
      if (subtreeCount == 0) {
        startBatch();
      } else if (batchSequenceNumber < maxFiles && subtreeCount >= batchSize) {
        endBatch();
        startBatch();
        subtreeCount = 0;
      }
      ++subtreeCount;
    } else {
      matched = false;
    }
    if (matched) {
      super.startElement(namespaceUri,localName,qname,atts);
      ++level;
    } else if (!started) {
      bufferBuilder.startElement(namespaceUri,localName,qname,atts);
    }
  }

  public void characters(char ch[], int start, int length) throws SAXException {
    if (level > 0) {
      super.characters(ch,start,length);
    } else if (!started) {
      bufferBuilder.characters(ch,start,length);
    }
  }

  public void endElement(String namespaceUri, String localName, String qname) throws SAXException {
    if (level > 0) {
      super.endElement(namespaceUri,localName,qname);
      --level;
    } else if (!started) {
      bufferBuilder.endElement(namespaceUri,localName,qname);
    }
  }

  public void startPrefixMapping (String prefix, String uri)
  throws SAXException {
    if (!started) {
      bufferBuilder.startPrefixMapping(prefix,uri);
    } else {
      super.startPrefixMapping(prefix,uri);
    }
  }

  public void endPrefixMapping (String prefix)
  throws SAXException {
    if (!started) {
      bufferBuilder.endPrefixMapping(prefix);
    } else {
      super.endPrefixMapping(prefix);
    }
  }

  private void endBatch() throws SAXException {
    //System.out.println(getClass().getName()+".endBatch");
    eventBuffer.closeEvents(getContentHandler());
    if (saxSink != null) {
      saxSink.close();
    }
  }
                                                                   
  private void startBatch() throws SAXException {
    //System.out.println(getClass().getName()+".startBatch");
    subtreeCount = 0;
    ++batchSequenceNumber;
    ParameterBuilder paramBuilder = new ParameterBuilder(flow.getParameters());
    paramBuilder.setLong(BATCH_SEQUENCE_NUMBER_NAME, batchSequenceNumber);
    Record newParameters = paramBuilder.toRecord();
    Flow newFlow = flow.replaceParameters(context, newParameters);
    saxSink = saxSinkFactory.createSaxSink(context, newFlow);
    saxSink.setOutputProperties(outputProperties);
    super.setContentHandler(saxSink.getContentHandler());
    eventBuffer.replayEvents(getContentHandler());
  }
}

