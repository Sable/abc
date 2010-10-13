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

package com.servingxml.io.saxsource;

import java.io.IOException;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;

import com.servingxml.util.xml.AbstractXmlReader;

/**
 * A <code>DefaultXmlReader</code> implement a SAX 2 <code>XMLReader</code> interface for supplying 
 * record field values as SAX events.
 *
 * 
 * @author  Daniel A. Parker
 */

public class DefaultXmlReader extends AbstractXmlReader {

  public DefaultXmlReader() {
  }

  public void parse(String systemId)
  throws IOException, SAXException {
    final ContentHandler handler = getContentHandler();
    handler.startDocument();
    handler.startElement("","wrap","wrap",EMPTY_ATTRIBUTES);
    handler.endElement("","wrap","wrap");
    handler.endDocument();
  }
}
