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

import java.io.IOException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.ErrorListener;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * A <code>DomSubtreeReader</code> implement a SAX 2 <code>XMLReader</code> interface for supplying 
 * DocumentFragment content as SAX events.
 *
 * 
 * @author  Daniel A. Parker
 */

public class NullXmlReader extends AbstractXmlReader 
implements XMLReader {

  public NullXmlReader() {
  }

  public void parse(String systemId)
  throws IOException, SAXException {
  }
}
