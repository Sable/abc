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

package com.servingxml.components.content.dynamic;

import java.util.HashMap;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.ContentHandler;

import com.servingxml.util.ServingXmlException;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public final class ContentWriter {

  private static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();
  private static final String emptyString = "";
  private final ContentHandler contentHandler;
  private final HashMap<String,String> prefixMap = new HashMap<String,String>();
  private int lastPrefixIndex = 0;

  public ContentWriter(ContentHandler contentHandler) {
    this.contentHandler = contentHandler;
  }

  public AttributeSet newAttributeSet() {
    return new AttributeSetImpl();
  }

  public ContentHandler getContentHandler() {
    return contentHandler;
  }

  public void processingInstruction(String target, String data) {
    try {
      contentHandler.processingInstruction(target,data);
    } catch (SAXException e){
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

  public final void startPrefixMapping(String prefix, String uri) {
    try {
      contentHandler.startPrefixMapping(prefix, uri);
      prefixMap.put(uri,prefix);
    } catch (SAXException e){
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

  public final void endPrefixMapping(String prefix) {
    try {
      contentHandler.endPrefixMapping(prefix);
      prefixMap.remove(prefix);
    } catch (SAXException e){
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

  private String getQname(String namespaceUri, String localName) {
    String qname;
    if (namespaceUri == null || namespaceUri.equals("")) {
      qname = localName;
    } else {
      String prefix = prefixMap.get(namespaceUri);
      if (prefix == null) {
        prefix = "ns" + ++lastPrefixIndex;
        prefixMap.put(namespaceUri,prefix);
      }
      qname = prefix + ":" + localName;
    }
    return qname;
  }

  public final void startElement(String namespaceUri, String localName) {
    try {
      String qname = getQname(namespaceUri,localName);

      contentHandler.startElement(namespaceUri, localName, qname, EMPTY_ATTRIBUTES);
    } catch (SAXException e){
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

  public final void startElement(String namespaceUri, String localName, AttributeSet attributes) {
    try {
      String qname = getQname(namespaceUri,localName);
      contentHandler.startElement(namespaceUri, localName, qname, attributes.getAttributes());
    } catch (SAXException e){
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

  public final void startEndElement(String namespaceUri, String localName) {
    startElement(namespaceUri, localName);
    endElement(namespaceUri, localName);
  }

  public final void startEndElement(String namespaceUri, String localName, 
                                    AttributeSet attributes) {
    startElement(namespaceUri, localName, attributes);
    endElement(namespaceUri, localName);
  }

  public final void endElement(String namespaceUri, String localName) {
    try {
      String qname = getQname(namespaceUri,localName);
      contentHandler.endElement(namespaceUri, localName, qname);
    } catch (SAXException e){
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

  public final void insertValue(String value) {
    try {
      char[] charArray = value.toCharArray();
      contentHandler.characters(charArray, 0, charArray.length);
    } catch (SAXException e){
      throw new ServingXmlException(e.getMessage(),e);
    }
  }     

  public final void insertValue(double value) {
    insertText(Double.toString(value));
  }

  public final void insertValue(float value) {
    insertText(Float.toString(value));
  }

  public final void insertValue(int value) {
    insertText(Integer.toString(value));
  }

  public final void insertValue(long value) {
    insertText(Long.toString(value));
  }

  public final void insertElement(String namespaceUri, String localName, String value) {
    startElement(namespaceUri,localName);
    insertValue(value);
    endElement(namespaceUri,localName);
  }

  public final void insertElement(String namespaceUri, String localName, float value) {
    startElement(namespaceUri,localName);
    insertValue(value);
    endElement(namespaceUri,localName);
  }

  public final void insertElement(String namespaceUri, String localName, double value) {
    startElement(namespaceUri,localName);
    insertValue(value);
    endElement(namespaceUri,localName);
  }

  public final void insertElement(String namespaceUri, String localName, int value) {
    startElement(namespaceUri,localName);
    insertValue(value);
    endElement(namespaceUri,localName);
  }

  public final void insertElement(String namespaceUri, String localName, long value) {
    startElement(namespaceUri,localName);
    insertValue(value);
    endElement(namespaceUri,localName);
  }

  public final void insertElement(String namespaceUri, String localName, 
  AttributeSet attributes, String value) {
    startElement(namespaceUri,localName, attributes);
    insertValue(value);
    endElement(namespaceUri,localName);
  }

  protected final void insertText(String value) {
    try {
      char[] charArray = value.toCharArray();
      contentHandler.characters(charArray, 0, charArray.length);
    } catch (SAXException e){
      throw new ServingXmlException(e.getMessage(),e);
    }
  }     
  /**
   * This class wraps a SAX2 Attributes implementation.  Its purpose
   * is to help with the construction of a SAX2 <code>Attributes</code> object. 
   *
   * 
   * @author Daniel A. Parker (daniel.parker@servingxml.com)
   */
    
  class AttributeSetImpl implements AttributeSet {
    private AttributesImpl attributes = new AttributesImpl();

    public void addAttribute(String namespaceUri, String localName, String value) {
      String qname = getQname(namespaceUri,localName);
      attributes.addAttribute(namespaceUri,localName, qname,"CDATA",value);
    }
  
    public void addAttribute(String namespaceUri, String localName, double value) {
      String qname = getQname(namespaceUri,localName);
      String s = Double.toString(value);
      attributes.addAttribute(namespaceUri,localName, qname,"CDATA",s);
    }
  
    public void addAttribute(String namespaceUri, String localName, float value) {
      String qname = getQname(namespaceUri,localName);
      String s = Float.toString(value);
      attributes.addAttribute(namespaceUri,localName, qname,"CDATA",s);
    }
  
    public void addAttribute(String namespaceUri, String localName, int value) {
      String qname = getQname(namespaceUri,localName);
      String s = Integer.toString(value);
      attributes.addAttribute(namespaceUri,localName, qname,"CDATA",s);
    }
  
    public void addAttribute(String namespaceUri, String localName, long value) {
      String qname = getQname(namespaceUri,localName);
      String s = Long.toString(value);
      attributes.addAttribute(namespaceUri,localName, qname,"CDATA",s);
    }
  
    public Attributes getAttributes() {
      return attributes;
    }
  }
}
