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

package com.servingxml.util.xml;

import org.xml.sax.ContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

import com.servingxml.util.Stack;
import com.servingxml.util.PrefixMap;
import com.servingxml.util.PrefixMapImpl;

/**
 * A <code>PrefixMappingXmlFilter</code> extends a SAX 2 <code>XMLFilterImpl</code> 
 *
 * 
 * @author  Daniel A. Parker
 */
                                
public class PrefixMappingXmlFilter extends XMLFilterImpl implements ExtendedContentHandler {
  private Stack<PrefixMap> stack = new Stack<PrefixMap>();

  private final LexicalHandler lexicalHandler;

  public PrefixMappingXmlFilter(ContentHandler contentHandler) {
    setContentHandler(contentHandler);
    this.lexicalHandler = null;
  }

  public PrefixMappingXmlFilter(ContentHandler contentHandler, LexicalHandler lexicalHandler) {
    setContentHandler(contentHandler);
    this.lexicalHandler = lexicalHandler;
  }

  public void startDocument() {
  }

  public void endDocument() {
  }

  public void startElement(String namespaceUri, String localName, String qname, Attributes atts) throws SAXException {

    PrefixMapImpl newPrefixMap = null;
    PrefixMap prefixMap;
    if (stack.size() > 0) {
      prefixMap = stack.peek();
    } else {
      newPrefixMap = new PrefixMapImpl();
      prefixMap = newPrefixMap;
    }

    if (namespaceUri.length() > 0) {
      String prefix = DomHelper.getPrefix(qname);
      if (!prefixMap.containsPrefixMapping(prefix,namespaceUri)) {
        if (newPrefixMap == null) {
          newPrefixMap = new PrefixMapImpl(prefixMap);
        }
        newPrefixMap.setPrefixMapping(prefix,namespaceUri);
        //super.startPrefixMapping(prefix,namespaceUri);
      }
      for (int i=0; i<atts.getLength(); i++) {
        String attUri = atts.getURI(i);
        if (attUri.length() > 0) {
          String attNamespaceUri = atts.getURI(i);
          String attLocalName = atts.getLocalName(i);
          String attQname = atts.getQName(i);
          String attPrefix = DomHelper.getPrefix(attQname);
          if (!prefixMap.containsPrefixMapping(attPrefix,attNamespaceUri)) {
            if (newPrefixMap == null) {
              newPrefixMap = new PrefixMapImpl(prefixMap);
            }
            newPrefixMap.setPrefixMapping(attPrefix,attNamespaceUri);
            //super.startPrefixMapping(attPrefix,attNamespaceUri);
          }
        }
      }
    }

    if (newPrefixMap != null) {
      prefixMap = newPrefixMap;
    }
    stack.push(prefixMap);
    super.startElement(namespaceUri,localName,qname,atts);
  }

  public void endElement(String namespaceUri, String localName, String qname) throws SAXException {
    super.endElement(namespaceUri,localName,qname);
    PrefixMap prefixMap = stack.pop();

    PrefixMap.PrefixMapping[] prefixDeclarations = prefixMap.getLocalPrefixDeclarations();
    for (int i = 0; i < prefixDeclarations.length; ++i) {
      PrefixMap.PrefixMapping prefixMapping = prefixDeclarations[i];
      //System.out.println(getClass().getName()+".generateElement prefix="+prefixMapping.getPrefix() + ", ns = " + prefixMapping.getNamespaceUri());
      super.endPrefixMapping(prefixMapping.getPrefix());
    }
  }    

  public void comment(char[] ch, int start, int length) throws SAXException {
    if (lexicalHandler != null) {
      lexicalHandler.comment(ch,start,length);
    }
  }
  public void endCDATA() throws SAXException
  {
    if (lexicalHandler != null) {
      lexicalHandler.endCDATA();
    }
  }
  public void endDTD() throws SAXException
  {
    if (lexicalHandler != null) {
      lexicalHandler.endDTD();
    }
  }
  public void endEntity(String name) throws SAXException
  {
    if (lexicalHandler != null) {
      lexicalHandler.endEntity(name);
    }
  }
  public void startCDATA() throws SAXException
  {
    if (lexicalHandler != null) {
      lexicalHandler.startCDATA();
    }
  }
  public void startDTD(String name, String publicId, String systemId) throws SAXException
  {
    if (lexicalHandler != null) {
      lexicalHandler.startDTD(name,publicId,systemId);
    }
  }
  public void startEntity(String name) throws SAXException {
    if (lexicalHandler != null) {
      lexicalHandler.startEntity(name);
    }
  }

  static class ElementEntry {
    final PrefixMap prefixMap;
    final String prefix;

    ElementEntry(PrefixMap prefixMap, String prefix) {
      this.prefixMap = prefixMap;
      this.prefix = prefix;
    }
  }
}
