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

package com.servingxml.util.record;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.servingxml.util.SystemConstants;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.Stack;
import com.servingxml.util.QualifiedName;

public class RecordContentHandler implements ContentHandler {

  private static final int INITIAL_STATE = 1;
  private static final int IGNORE_STATE = 2;
  private static final int RECORD_STATE = 3;
  private static final int FIELD_STATE = 4;

  private final Name recordTypeName;
  private final RecordReceiver recordReceiver;
  private Stack<StackEntry> stack = new Stack<StackEntry>();

  public RecordContentHandler(Name recordTypeName, RecordReceiver recordReceiver) {
    this.recordTypeName = recordTypeName;
    this.recordReceiver = recordReceiver;
  }

  public void startDocument() throws SAXException {
    stack = new Stack<StackEntry>();
    stack.push(StackEntry.INITIAL_ENTRY);
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

  public void startElement(String namespaceUri, String localName, String qname, 
    Attributes atts) throws SAXException {
    //System.out.println(getClass().getName()+".startElement qname="+qname);

    final StackEntry parentEntry = stack.peek();
    int state = parentEntry.state();
    if (state == INITIAL_STATE && 
      namespaceUri.equals(SystemConstants.SERVINGXML_NS_URI) && 
      localName.equals("records")) {
      stack.push(StackEntry.INITIAL_ENTRY);
    } else if (state == INITIAL_STATE 
               && 
      namespaceUri.equals(recordTypeName.getNamespaceUri()) && 
      localName.equals(recordTypeName.getLocalName())
               ) {
      Name recordTypeName = new QualifiedName(namespaceUri,localName);
      stack.push(new RecordEntry(recordTypeName, recordReceiver));
    } else if (state == RECORD_STATE) {
      Name fieldName = new QualifiedName(namespaceUri,localName);
      stack.push(new FieldEntry(fieldName,(RecordEntry)parentEntry));
    } else if (state == FIELD_STATE) {
      //System.out.println(getClass().getName()+". recordType=" + localName);
      Name recordTypeName = new QualifiedName(namespaceUri,localName);
      stack.push(new RecordEntry(recordTypeName, (FieldEntry)parentEntry));
    } else {
      stack.push(StackEntry.INITIAL_ENTRY);
    }

    //System.out.println(getClass().getName()+".startElement size=" + stack.size());
  }

  public void characters(char ch[], int start, int length) throws SAXException {
    StackEntry stackEntry = stack.peek();
    stackEntry.characters(ch,start,length);
  }

  public void endElement(String namespaceUri, String localName, String qname) throws SAXException {
    //System.out.println(getClass().getName()+".endElement qname="+qname);
    StackEntry stackEntry = stack.pop();
    stackEntry.endElement();
  }

  public void endDocument() throws SAXException {    
    stack.pop();
  }

  interface StackEntry {
    static StackEntry INITIAL_ENTRY = new InitialEntry();

    int state();
    void characters(char ch[], int start, int length);
    void endElement() throws SAXException;
  }

  static class InitialEntry implements StackEntry {

    public int state() {
      return INITIAL_STATE;
    }

    public void characters(char ch[], int start, int length) {
    }

    public void endElement() {
    }
  }

  static class FieldEntry implements StackEntry, RecordReceiver {
    final RecordEntry recordEntry;
    final Name fieldName;
    final StringBuilder buffer;

    FieldEntry(Name fieldName, RecordEntry recordEntry) {
      this.fieldName = fieldName;
      this.recordEntry = recordEntry;
      this.buffer = new StringBuilder();
    }

    public int state() {
      return FIELD_STATE;
    }

    public void receiveRecord(Record segment) {
      //System.out.println(getClass().getName()+"receiveRecord field="+fieldName+", recordType=" + segment.getRecordType().getName());
      recordEntry.addSegment(fieldName,segment);
    }

    public void characters(char ch[], int start, int length) {
      String s = new String(ch,start,length);
      buffer.append(ch,start,length);
    }

    public void endElement() {
      String value = buffer.toString();
      recordEntry.addString(fieldName,value);
    }
  }

  static class RecordEntry implements StackEntry {
    final MultivaluedFieldBuilder fieldBuilder;
    final Name recordTypeName;
    final RecordReceiver recordReceiver;

    RecordEntry(Name recordTypeName, RecordReceiver recordReceiver) {
      this.recordTypeName = recordTypeName;
      this.fieldBuilder = new MultivaluedFieldBuilder();
      this.recordReceiver = recordReceiver;
    }

    public int state() {
      return RECORD_STATE;
    }

    public void characters(char ch[], int start, int length) {
    }

    public void addString(Name fieldName, String s) {
      fieldBuilder.addString(fieldName,s);
    }

    public void addSegment(Name fieldName, Record segment) {
      fieldBuilder.addSegment(fieldName, segment);
    }

    public void endElement() throws SAXException {
      try {
        RecordBuilder recordBuilder = new RecordBuilder(recordTypeName);
        fieldBuilder.updateRecord(recordBuilder);
        Record record = recordBuilder.toRecord();
        recordReceiver.receiveRecord(record);
        fieldBuilder.clear();
      } catch (ServingXmlException e) {
        throw new SAXException(e.getMessage(),e);
      }
    }
  }
}

