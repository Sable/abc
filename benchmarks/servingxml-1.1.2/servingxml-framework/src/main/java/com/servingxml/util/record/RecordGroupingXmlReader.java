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

import java.io.IOException;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;
import com.servingxml.util.PrefixMap;
import com.servingxml.util.PrefixMapImpl;

import com.servingxml.util.SystemConstants;
import com.servingxml.util.xml.AbstractXmlReader;
import com.servingxml.util.Name;

/**
 * A <code>RecordGroupingXmlReader</code> implement a SAX 2 <code>XMLReader</code> interface for supplying 
 * record field values as SAX events.
 *
 * 
 * @author  Daniel A. Parker
 */

public class RecordGroupingXmlReader extends AbstractXmlReader {

  private final PrefixMap prefixMap;
  private final Record prevRecord;
  private final Record currentRecord;
  private final String namespaceUri;
  private final String localName;

  public RecordGroupingXmlReader(PrefixMap prefixMap, Record prevRecord, Record currentRecord) {
    this.prefixMap = prefixMap;
    this.namespaceUri = SystemConstants.SERVINGXML_NS_URI;
    this.localName = "adjacentRecords";
    this.prevRecord = prevRecord;
    this.currentRecord = currentRecord;
  }

  public void parse(String systemId)
  throws IOException, SAXException {

    getContentHandler().startDocument();
    final ContentHandler handler = getContentHandler();

    String qname = generateQname(namespaceUri, localName);
    handler.startElement(namespaceUri,localName,qname,SystemConstants.EMPTY_ATTRIBUTES);

    if (prevRecord != null) {
      handler.startElement(SystemConstants.SERVINGXML_NS_URI,"previous","sx:previous",SystemConstants.EMPTY_ATTRIBUTES);
      prevRecord.writeToContentHandler(prefixMap,handler);
      handler.endElement(SystemConstants.SERVINGXML_NS_URI,"previous","sx:previous");
    }

    if (currentRecord != null) {
      handler.startElement(SystemConstants.SERVINGXML_NS_URI,"current","sx:current",SystemConstants.EMPTY_ATTRIBUTES);
      currentRecord.writeToContentHandler(prefixMap,handler);
      handler.endElement(SystemConstants.SERVINGXML_NS_URI,"current","sx:current");
    }

    handler.endElement(namespaceUri,localName,qname);
    handler.endDocument();
  }
}
