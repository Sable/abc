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

package com.servingxml.components.saxsource;

import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.SAXException;

import com.servingxml.util.Name;

public class SaxEventBufferImpl implements SaxEventBuffer {
  private final int[] symbols;
  private final char[] values;
  private final Name[] names;

  public SaxEventBufferImpl(Name[] names,int[] symbols,char[] values) {
    this.names = names;
    this.symbols = symbols;
    this.values = values;
  }

  public XMLReader createXmlReader() {
    return new SaxEventBufferReader(this);
  }

  public void replayEvents(ContentHandler handler) throws SAXException {
    for (int i = 0; i < symbols.length; ++i) {
      int symbol = symbols[i];
      if (symbol == SaxEventBuffer.START_DOCUMENT) {
        handler.startDocument();
      } else if (symbol == SaxEventBuffer.START_PREFIX_MAPPING) {
        int prefixMapId = symbols[++i];
        Name prefixMap = names[prefixMapId];
        //System.out.println("out:  startPrefixMapping" + prefixMap.getLocalName()+":" + prefixMap.getNamespaceUri());
        handler.startPrefixMapping(prefixMap.getLocalName(), prefixMap.getNamespaceUri());
      } else if (symbol == SaxEventBuffer.END_PREFIX_MAPPING) {
        int start = symbols[++i];
        int length = symbols[++i];
        String prefix = new String(values,start,length);
        //System.out.println("out:  endPrefixMapping " + prefix);
        handler.endPrefixMapping(prefix);
      } else if (symbol == SaxEventBuffer.START_ELEMENT) {
        int elementNameId = symbols[++i];
        Name elementName = names[elementNameId];
        int start = symbols[++i];
        int length = symbols[++i];
        String qname = new String(values,start,length);
        //System.out.println("out:  startElement " + qname);
        int attributeCount = symbols[++i];

        AttributesImpl atts = new AttributesImpl();
        for (int j = 0; j < attributeCount; ++j) {
          int attNameId = symbols[++i];
          Name attName = names[attNameId];
          int attQnameStart = symbols[++i];
          int attQnameLength = symbols[++i];
          String attQname = new String(values,attQnameStart,attQnameLength);
          int attTypeStart = symbols[++i];
          int attTypeLength = symbols[++i];
          String type = new String(values,attTypeStart,attTypeLength);
          int attValueStart = symbols[++i];
          int attValueLength = symbols[++i];
          String attValue = new String(values,attValueStart,attValueLength);
          atts.addAttribute(attName.getNamespaceUri(),attName.getLocalName(),
            attQname,type,attValue);
        }
        String ns = elementName.getNamespaceUri();
        //System.out.println(getClass().getName()+".parse " + qname + ", ns=" +ns);
        handler.startElement(ns, elementName.getLocalName(),qname,atts);
      } else if (symbol == SaxEventBuffer.CHARACTERS) {
        int chStart = symbols[++i];
        int chLength = symbols[++i];
        handler.characters(values,chStart,chLength);
        String chValue = new String(values,chStart,chLength);
      } else if (symbol == SaxEventBuffer.IGNORABLE_WHITESPACE) {
        int chStart = symbols[++i];
        int chLength = symbols[++i];
        handler.ignorableWhitespace(values,chStart,chLength);
        String chValue = new String(values,chStart,chLength);
      } else if (symbol == SaxEventBuffer.END_ELEMENT) {
        int elementNameId = symbols[++i];
        Name elementName = names[elementNameId];
        int start = symbols[++i];
        int length = symbols[++i];
        String qname = new String(values,start,length);
        handler.endElement(elementName.getNamespaceUri(),elementName.getLocalName(),
          qname);
      } else if (symbol == SaxEventBuffer.END_DOCUMENT) {
        handler.endDocument();
      }
    }
  }

  public void closeEvents(ContentHandler handler) throws SAXException {
    //System.out.println(getClass()+".closeEvents");

    int[] stack = new int[symbols.length];
    int count = 0;
    for (int i = 0; i < symbols.length; ++i) {
      int symbol = symbols[i];
      if (symbol == SaxEventBuffer.START_DOCUMENT) {
      } else if (symbol == SaxEventBuffer.START_PREFIX_MAPPING) {
        int prefixMapId = symbols[++i];
        Name prefixMap = names[prefixMapId];
        //System.out.println("out:  startPrefixMapping" + prefixMap.getLocalName()+":" + prefixMap.getNamespaceUri());
      } else if (symbol == SaxEventBuffer.END_PREFIX_MAPPING) {
        int start = symbols[++i];
        int length = symbols[++i];
        String prefix = new String(values,start,length);
        //System.out.println("out:  endPrefixMapping " + prefix);
      } else if (symbol == SaxEventBuffer.START_ELEMENT) {
        stack[count++] = i;
        int elementNameId = symbols[++i];
        Name elementName = names[elementNameId];
        int start = symbols[++i];
        int length = symbols[++i];
        String qname = new String(values,start,length);
        //System.out.println("out:  startElement " + qname);
        int attributeCount = symbols[++i];

        AttributesImpl atts = new AttributesImpl();
        for (int j = 0; j < attributeCount; ++j) {
          int attNameId = symbols[++i];
          Name attName = names[attNameId];
          int attQnameStart = symbols[++i];
          int attQnameLength = symbols[++i];
          String attQname = new String(values,attQnameStart,attQnameLength);
          int attTypeStart = symbols[++i];
          int attTypeLength = symbols[++i];
          String type = new String(values,attTypeStart,attTypeLength);
          int attValueStart = symbols[++i];
          int attValueLength = symbols[++i];
          String attValue = new String(values,attValueStart,attValueLength);
          atts.addAttribute(attName.getNamespaceUri(),attName.getLocalName(),
            attQname,type,attValue);
        }
        String ns = elementName.getNamespaceUri();
        //System.out.println("startElement " + qname);
        //System.out.println(getClass().getName()+".parse " + qname + ", ns=" +ns);
      } else if (symbol == SaxEventBuffer.CHARACTERS) {
        int chStart = symbols[++i];
        int chLength = symbols[++i];
        String chValue = new String(values,chStart,chLength);
      } else if (symbol == SaxEventBuffer.IGNORABLE_WHITESPACE) {
        int chStart = symbols[++i];
        int chLength = symbols[++i];
        String chValue = new String(values,chStart,chLength);
      } else if (symbol == SaxEventBuffer.END_ELEMENT) {
        --count;
        int elementNameId = symbols[++i];
        Name elementName = names[elementNameId];
        int start = symbols[++i];
        int length = symbols[++i];
        String qname = new String(values,start,length);
        //System.out.println("endElement " + qname);
      } else if (symbol == SaxEventBuffer.END_DOCUMENT) {
      }
    }

    //System.out.println(getClass()+".closeEvents count=" + count);
    if (count > 0) {
      for (int i = count-1; i >= 0; --i) {
        int j = stack[i];
        int symbol = symbols[j];
        int elementNameId = symbols[++j];
        Name elementName = names[elementNameId];
        int start = symbols[++j];
        int length = symbols[++j];
        String qname = new String(values,start,length);
        //System.out.println(getClass()+".closeEvents end element " + qname);
        handler.endElement(elementName.getNamespaceUri(),elementName.getLocalName(),qname);
      }
      handler.endDocument();
    }
  }
}

