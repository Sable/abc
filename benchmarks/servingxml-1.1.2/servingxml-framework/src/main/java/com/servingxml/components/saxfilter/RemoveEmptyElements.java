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

import java.util.LinkedList;

import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.xml.sax.XMLFilter;

import com.servingxml.util.NameTest;

public class RemoveEmptyElements extends XMLFilterImpl implements XMLFilter {
  private static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();

  private final NameTest elements;
  private final NameTest except;
  private final boolean allDescendents;
  private LinkedList<StackEntry> list;

  public RemoveEmptyElements(NameTest elements) {
    this.elements = elements;
    this.except = NameTest.NONE;
    this.allDescendents = false;
  }

  public RemoveEmptyElements(NameTest elements, boolean allDescendents) {
    this.elements = elements;
    this.except = NameTest.NONE;
    this.allDescendents = allDescendents;
  }

  public RemoveEmptyElements(NameTest elements, NameTest except) {
    this.elements = elements;
    this.except = except;
    this.allDescendents = false;
  }

  public RemoveEmptyElements(NameTest elements, NameTest except, boolean allDescendents) {
    this.elements = elements;
    this.except = except;
    this.allDescendents = allDescendents;
  }

  public void startDocument() throws SAXException {
    list = new LinkedList<StackEntry>();
    super.startDocument();
  }

  public void startElement(String namespaceUri, String localName, String qname, Attributes atts)
  throws SAXException {
    if (allDescendents && atts.getLength() == 0 && list.size() > 0) {
      list.add(new StackEntry(namespaceUri,localName,qname));
    } else if (atts.getLength() == 0 && elements.matches(namespaceUri,localName) && !except.matches(namespaceUri,localName)) {
      list.add(new StackEntry(namespaceUri,localName,qname));
    } else {
      while (list.size() > 0) {
        StackEntry entry = list.removeFirst();
        super.startElement(entry.namespaceUri,entry.localName,entry.qname,EMPTY_ATTRIBUTES);
      }
      super.startElement(namespaceUri,localName,qname,atts);
    }
  }

  public void endElement(String namespaceUri, String localName, String qname)
  throws SAXException {
    if (list.size() > 0) {
      list.removeLast();
    } else {
      super.endElement(namespaceUri,localName,qname);
    }
  }

  public void characters(char[] ch, int start, int length) 
  throws SAXException {
    if (length == 0 || new String(ch,start,length).trim().length() == 0) {
    } else {
      while (list.size() > 0) {
        StackEntry entry = list.removeFirst();
        super.startElement(entry.namespaceUri,entry.localName,entry.qname,EMPTY_ATTRIBUTES);
      }
      super.characters(ch,start,length);
    }
  }

  class StackEntry {
    final String namespaceUri;
    final String localName;
    final String qname;

    StackEntry(String namespaceUri, String localName, String qname) {
      this.namespaceUri = namespaceUri;
      this.localName = localName;
      this.qname = qname;
    }
  }
}


