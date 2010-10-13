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

package com.servingxml.components.wrap;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;

import com.servingxml.io.saxsink.SaxSink;
import com.servingxml.io.saxsink.SimpleSaxSink;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.Stack;
import com.servingxml.util.Stack;
import com.servingxml.util.xml.ChainedContentHandler;
import com.servingxml.util.xml.NullContentHandler;

public class WrapFilter extends ChainedContentHandler  {
  private Stack<PrefixMapping> prefixMappingStack = new Stack<PrefixMapping>();
  private int depth = 0;
  private PrefixMapping current = PrefixMapping.NULL;

  public WrapFilter(ContentHandler handler) {
    super(handler);
  }

  interface PrefixMapping {
    int UNMATCHED = 0;
    int MATCHED_PREFIX = 1;
    int MATCHED_PREFIX_AND_URI = 2;
    PrefixMapping NULL = new NullPrefixMapping();

    int findPrefixMap(String prefix, String uri);
  }
  static class NullPrefixMapping implements PrefixMapping {
    public int findPrefixMap(String prefix, String uri) {
      return UNMATCHED;
    }
  }

  static class PrefixUriPair implements PrefixMapping {
    int UNMATCHED = 0;
    int MATCHED_PREFIX = 1;
    int MATCHED_PREFIX_AND_URI = 2;

    private final String prefix;
    private final String uri;
    private final PrefixMapping tail;

    PrefixUriPair(String prefix, String uri) {
      this.prefix = prefix;
      this.uri = uri;
      this.tail = null;
    }

    PrefixUriPair(String prefix, String uri, PrefixMapping tail) {
      this.prefix = prefix;
      this.uri = uri;
      this.tail = tail;
    }

    public int findPrefixMap(String prefix, String uri) {
      int result = UNMATCHED;
      if (this.prefix.equals(prefix)) {
        result = MATCHED_PREFIX;
        if (this.uri.equals(uri)) {
          result = MATCHED_PREFIX_AND_URI;
        }
      } else if (tail != null) {
        result = tail.findPrefixMap(prefix,uri);
      }
      return result;
    }
  }

  boolean findPrefixMap(String prefix, String namespace) {
    boolean found = false;
    boolean done = false;

    int result = current.findPrefixMap(prefix,namespace);
    if (result != PrefixMapping.UNMATCHED) {
      done = true;
    }
    if (result == PrefixMapping.MATCHED_PREFIX_AND_URI) {
      found = true;
    }

    for (int i = 0; !done && i < prefixMappingStack.size(); ++i) {
      PrefixMapping pair = prefixMappingStack.peek(i);
      result = pair.findPrefixMap(prefix,namespace);
      if (result != PrefixMapping.UNMATCHED) {
        done = true;
      }
      if (result == PrefixMapping.MATCHED_PREFIX_AND_URI) {
        found = true;
      }
    }
    return found;
  }

  public void startDocument() throws SAXException {
    if (depth == 0) {
      super.startDocument();
    }
  }

  public void endDocument() throws SAXException {
    if (depth == 0) {
      super.endDocument();
    }
  }

  public void startElement(String namespaceUri, String localName, String qname, Attributes atts)
  throws SAXException {
    //System.out.println(getClass().getName()+".startElement enter " + qname);
    prefixMappingStack.push(current);
    current = PrefixMapping.NULL;
    //System.out.println(getClass().getName()+".startElement enter " + qname);
    super.startElement(namespaceUri,localName,qname,atts);
    //System.out.println(getClass().getName()+".startElement leave " + qname);
    ++depth;
  }

  public void endElement(String namespaceUri, String localName, String qname)
  throws SAXException {
    //System.out.println(getClass().getName()+".endElement enter " + qname);
    --depth;
    prefixMappingStack.pop();
    //System.out.println(getClass().getName()+".endElement enter " + qname);
    super.endElement(namespaceUri,localName,qname);
    //System.out.println(getClass().getName()+".endElement leave " + qname);
  }

  public void startPrefixMapping(String prefix, String uri)
  throws SAXException {
   //System.out.println(getClass().getName()+".startPrefixMapping " + prefix + ":"+uri);
    if (!findPrefixMap(prefix,uri)) {
      super.startPrefixMapping(prefix, uri);
      current = new PrefixUriPair(prefix,uri,current);
    }
  }

  public void endPrefixMapping(String prefix)
  throws SAXException {
  }
}


