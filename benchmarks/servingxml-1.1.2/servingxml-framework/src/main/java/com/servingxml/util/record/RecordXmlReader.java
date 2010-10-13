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
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.servingxml.util.PrefixMap;
import com.servingxml.util.PrefixMapImpl;
import com.servingxml.util.xml.AbstractXmlReader;
import com.servingxml.util.xml.PrefixMappingXmlFilter;

/**
 * A <code>RecordXmlReader</code> implement a SAX 2 <code>XMLReader</code> interface for supplying 
 * record field values as SAX events.
 *
 * 
 * @author  Daniel A. Parker
 */

public class RecordXmlReader extends AbstractXmlReader {

  private final PrefixMap prefixMap;
  private final Record record;

  public RecordXmlReader(Record record, PrefixMap prefixMap) {
    this.record = record;
    this.prefixMap = prefixMap;
  }

  public void parse(String systemId)
  throws IOException, SAXException {
    //System.out.println(record.getClass().getName() + ".parse start");

    final ContentHandler handler = getContentHandler();
    handler.startDocument();
    //System.out.println(record.getClass().getName() + ".parse before writeToContentHandler");
    record.writeToContentHandler(prefixMap, handler);
    handler.endDocument();
    //System.out.println(record.getClass().getName() + ".parse end");
  }
}
