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

package com.servingxml.components.inverserecordmapping;

import java.util.Properties;

import org.xml.sax.ContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.Locator; 

import com.servingxml.app.ServiceContext;
import com.servingxml.components.recordio.RecordReader;
import com.servingxml.components.recordio.RecordWriter;
import com.servingxml.components.recordio.AbstractRecordReader;
import com.servingxml.app.xmlpipeline.XmlPipeline;
import com.servingxml.components.content.XmlPipelineFactory;
import com.servingxml.expr.saxpath.SaxPath;
import com.servingxml.io.cache.DefaultKey;
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.Key;
import com.servingxml.app.Flow;
import com.servingxml.util.Stack;
import com.servingxml.util.record.Record;

//DEBUG
import com.servingxml.util.xml.ContentHandlerFilter;

public class SubtreeRecordReader extends AbstractRecordReader implements ContentHandler, RecordReader {
  private Record current = null;
  private RecordWriter recordWriter = RecordWriter.NULL;
  private RecordWriter discardWriter = RecordWriter.DEFAULT_DISCARD_WRITER;
  private final XmlPipelineFactory pipelineFactory;
  private final InverseRecordMapping inverseRecordMapping;
  private ServiceContext context = null;
  private Flow flow = null;
  private final Properties defaultOutputProperties;
  private final Key key;
  private ShredXml flattener;
  private Stack<SaxPath> stack = new Stack<SaxPath>();

  public SubtreeRecordReader(XmlPipelineFactory pipelineFactory, 
    Properties defaultOutputProperties,
    InverseRecordMapping inverseRecordMapping) {                            
    this.pipelineFactory = pipelineFactory;
    this.defaultOutputProperties = defaultOutputProperties;
    this.inverseRecordMapping = inverseRecordMapping;
    this.key = DefaultKey.newInstance();
  }

  public RecordWriter getRecordWriter() {
    return recordWriter;
  }

  public void setRecordWriter(RecordWriter recordWriter) {
    this.recordWriter = recordWriter;
  }

  public RecordWriter getDiscardWriter() {
    return discardWriter;
  }

  public void setDiscardWriter(RecordWriter discardWriter) {
    this.discardWriter = discardWriter;
  }

  public void close() {
  }

  public Key getKey() {
    return key;         
  }

  public Expirable getExpirable() {
    return Expirable.IMMEDIATE_EXPIRY;
  }

  public void readRecords(ServiceContext context, Flow flow) {
    //System.out.println(getClass().getName()+".readRecords " + defaultOutputProperties.getProperty("indent"));
    this.context = context;
    this.flow = flow;
    this.flattener = inverseRecordMapping.createShredXml(context, flow);

    try {
      recordWriter.startRecordStream(context, flow);

      XmlPipeline pipeline = pipelineFactory.createPipeline(context,flow,defaultOutputProperties);

      //  DEBUG
      /* ContentHandler handler = new ContentHandlerFilter(this) {
          public void startDocument() throws SAXException {
            //System.out.println(getClass().getName()+".startDocument");
            super.startDocument();
          }
          public void endDocument() throws SAXException {
            //System.out.println(getClass().getName()+".endDocument");
            super.endDocument();
          }
      }; */
      pipeline.execute(this);
      recordWriter.endRecordStream(context, flow);
    } finally {
      try {
        recordWriter.close();
      } catch (Exception e) {
        //  Dont' care
      }
    }
  }

  public void startDocument() throws SAXException {
    stack = new Stack();
  }

  public void endDocument() throws SAXException {
  }

  public void startElement(String namespaceUri, String localName, String qname, 
    Attributes atts) throws SAXException {
    SaxPath parent = stack.empty() ? null : stack.peek();

    SaxPath path;
    if (parent == null) {
      path = new SaxPath(context.getNameTable(),namespaceUri,localName,qname,
                         atts);
    } else {
      path = new SaxPath(namespaceUri,localName,qname,atts,parent);
    }
    stack.push(path);

    flattener.matchPath(context,flow,path);
    if (flattener.isMatched()) {
      flattener.startElement(context, flow, path, recordWriter);
    }
  }

  public void characters(char ch[], int start, int length) throws SAXException {
    flattener.characters(ch,start,length);
  }

  public void endElement(String namespaceUri, String localName, String qname)
  throws SAXException {

    flattener.endElement(context, flow, namespaceUri,localName,qname, recordWriter);
    stack.pop();
  }

  public void setDocumentLocator(Locator locator) {
  }

  public void startPrefixMapping(String prefix, String uri) throws SAXException {
  }
  public void endPrefixMapping(String prefix) throws SAXException {
  }
  public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
  }
  public void processingInstruction(String target, String data) throws SAXException {
  }
  public void skippedEntity(String name) throws SAXException {
  }

  public void warning (SAXParseException exception)
  throws SAXException {

    //if (errorDetail.size() > maxErrors) {
    throw exception;
    //}
  }

  public void error(SAXParseException exception)
  throws SAXException {
    throw exception;
  }

  public void fatalError(SAXParseException exception)
  throws SAXException {
    throw exception;
  }
}

