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

import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.util.MutableNameTable;
import com.servingxml.util.Name;
import com.servingxml.util.NameTableImpl;
import com.servingxml.util.xml.DomHelper;
import com.servingxml.util.ServingXmlException;
import java.io.StringWriter;
import java.io.Writer;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class SaxEventBufferBuilder implements ContentHandler {

  private static final int INITIAL_SYMBOL_SIZE = 100;
  private static final int INITIAL_VALUE_SIZE = 100;

  private int[] symbols = new int[INITIAL_SYMBOL_SIZE];
  private int symbolCount = 0;
  private char[] values = new char[INITIAL_VALUE_SIZE];
  private int valueCount = 0;
  private MutableNameTable nameTable = new NameTableImpl();

  public SaxEventBufferBuilder() {
  }

  public void addSymbol(int event) {
    if (symbolCount >= symbols.length) {
      int[] oldEvents = symbols;
      int size = oldEvents.length*2;
      if (size < INITIAL_SYMBOL_SIZE) {
        size = INITIAL_SYMBOL_SIZE;
      }
      symbols = new int[size];
      if (symbolCount > 0) {
        System.arraycopy(oldEvents,0,symbols,0,symbolCount);
      }
    }
    symbols[symbolCount] = event;
    ++symbolCount;
  }

  public void addValue(char[] ch, int start, int length) {
    addSymbol(valueCount);
    addSymbol(length);

    if (valueCount+length > values.length) {
      char[] oldValues = values;
      int size = (oldValues.length + length)*2;
      if (size < INITIAL_SYMBOL_SIZE) {
        size = INITIAL_SYMBOL_SIZE;
      }
      values = new char[size];
      if (valueCount > 0) {
        System.arraycopy(oldValues,0,values,0,valueCount);
      }
    }
    System.arraycopy(ch,start,values,valueCount,length);
    valueCount += length;
  }

  public void addValue(String s) {
    addValue(s.toCharArray(),0,s.length());
  }

  public void startDocument() throws SAXException {
    try {
      addSymbol(SaxEventBuffer.START_DOCUMENT);
    } catch (Exception e) {
      throw new SAXException(e.getMessage(),e);
    }
  }

  public void setDocumentLocator(Locator locator) {
  }

  public void startPrefixMapping (String prefix, String uri)
  throws SAXException {
    //System.out.println("in:  startPrefixMapping" + prefix+":" + uri);
    addSymbol(SaxEventBuffer.START_PREFIX_MAPPING);
    int nameSymbol = nameTable.getSymbol(uri,prefix);
    addSymbol(nameSymbol);
  }

  public void endPrefixMapping (String prefix)
  throws SAXException {
    //System.out.println("in:  endPrefixMapping " + prefix);
    addSymbol(SaxEventBuffer.END_PREFIX_MAPPING);
    addValue(prefix);
  }

  public void ignorableWhitespace (char ch[], int start, int length)
  throws SAXException {
    addSymbol(SaxEventBuffer.IGNORABLE_WHITESPACE);
    addValue(ch,start,length);
  }
  
  public void processingInstruction (String target, String data)
  throws SAXException {
  }
  public void skippedEntity (String name)
  throws SAXException {
  }
  public void startElement(String namespaceUri, String localName, String qname, 
  Attributes atts) throws SAXException {
    //System.out.println(getClass().getName()+".startElement ns=" + namespaceUri + ", name=" + localName + ", qname=" + qname);
    addSymbol(SaxEventBuffer.START_ELEMENT);
    int nameSymbol = nameTable.getSymbol(namespaceUri,localName);
    addSymbol(nameSymbol);
    addValue(qname);
    addSymbol(atts.getLength());
    for (int i = 0; i < atts.getLength(); ++i) {
      String attPrefix = DomHelper.getPrefix(atts.getQName(i));
      int attNameId = nameTable.getSymbol(atts.getURI(i),atts.getLocalName(i));
      addSymbol(attNameId);
      addValue(atts.getQName(i));
      addValue(atts.getType(i));
      String s = atts.getValue(i);
      addValue(s);
    }
  }
  public void characters(char ch[], int start, int length) throws SAXException {
    addSymbol(SaxEventBuffer.CHARACTERS);
    addValue(ch,start,length);
  }

  public void endElement(String namespaceUri, String localName, String qname) throws SAXException {
    addSymbol(SaxEventBuffer.END_ELEMENT);
    int nameSymbol = nameTable.getSymbol(namespaceUri,localName);
    addSymbol(nameSymbol);
    addValue(qname);
  }

  public void endDocument() throws SAXException {
    addSymbol(SaxEventBuffer.END_DOCUMENT);
  }

  public SaxEventBuffer getBuffer() {
    Name[] names = nameTable.getNames();
    int[] theSymbols = new int[symbolCount];
    System.arraycopy(symbols,0,theSymbols,0,symbolCount);
    char[] theValues = new char[valueCount];
    System.arraycopy(values,0,theValues,0,valueCount);

    SaxEventBuffer saxEventBuffer = new SaxEventBufferImpl(names,theSymbols,theValues);
    return saxEventBuffer;
  }

  public String toString() {
    try {
      SaxEventBuffer buffer = getBuffer();
      Writer writer = new StringWriter();
      Result result = new StreamResult(writer);
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      SaxSource saxSource = new SaxEventBufferSource(buffer,"", transformerFactory);
      XMLReader reader = saxSource.createXmlReader();
      Source xmlSource = new SAXSource(reader,new InputSource(""));
      transformer.transform(xmlSource,result);
      String s = writer.toString();
      return s;
    } catch (ServingXmlException e) {
      throw e;
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(), e);
    }
  }
}

