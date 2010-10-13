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

package com.servingxml.util;

import java.io.IOException;
import java.util.Iterator;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;

import com.servingxml.util.xml.AbstractXmlReader;

/**
 * A <code>ServingXmlFaultReader</code> implement a XMLReader interface for feeding
 * start/end elements back to an application requesting an XML document.
 *
 * 
 * @author  Daniel A. Parker
 */

class ServingXmlFaultReader extends AbstractXmlReader 
implements XMLReader {

  private final Name code;
  private final Name[] subcodes;
  private final String reason;
  private final ServingXmlFaultDetail detail;

  public ServingXmlFaultReader(Name code, String reason) {
    this.code = code;
    this.subcodes = new Name[0];
    this.reason = reason;
    this.detail = new ServingXmlFaultDetail();
  }

  public ServingXmlFaultReader(Name code, Name[] subcodes, String reason, ServingXmlFaultDetail detail) {
    this.code = code;
    this.subcodes = subcodes;
    this.reason = reason;
    this.detail = detail;
  }

  public void parse(String systemId)
  throws IOException, SAXException {

    ContentHandler handler = getContentHandler();
    handler.startDocument();

    handler.startPrefixMapping("env",code.getNamespaceUri());

    handler.startElement(code.getNamespaceUri(),"Fault","env:Fault",EMPTY_ATTRIBUTES);
    handler.startElement(code.getNamespaceUri(),"Code","env:Code",EMPTY_ATTRIBUTES);

    String s = "env" + ":" + code.getLocalName();
    char[] a = s.toCharArray();
    handler.characters(a,0,a.length);
    for (int i = 0; i < subcodes.length; ++i) {
      Name subcode = subcodes[i];
    }
    handler.endElement(code.getNamespaceUri(),"Code","env:Code");
    handler.startElement(code.getNamespaceUri(),"Reason","env:Reason",EMPTY_ATTRIBUTES);
    char[] reasonCh = reason.toCharArray();
    handler.characters(reasonCh,0,reasonCh.length);
    handler.endElement(code.getNamespaceUri(),"Reason","env:Reason");
    if (detail.size() > 0) {
      handler.startElement(code.getNamespaceUri(),"Detail","env:Detail",EMPTY_ATTRIBUTES);
      Iterator<ServingXmlFaultDetail.Entry> iter = detail.entries();
      while (iter.hasNext()) {
        ServingXmlFaultDetail.Entry entry = iter.next();
        String ns = entry.getName().getNamespaceUri();
        String localPart = entry.getName().getLocalName();
        String qname = generateQname(ns,localPart);
        handler.startElement(ns,localPart,qname,EMPTY_ATTRIBUTES);
        char[] messageCh = entry.getMessage().toCharArray();
        handler.characters(messageCh,0,messageCh.length);
        handler.endElement(ns,localPart,qname);
      }
      handler.endElement(code.getNamespaceUri(),"Detail","env:Detail");
    }
    handler.endElement(code.getNamespaceUri(),"Fault","env:Fault");
    handler.endDocument();
  }
}
