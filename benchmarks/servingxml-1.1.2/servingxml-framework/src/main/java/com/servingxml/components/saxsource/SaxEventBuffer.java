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
import org.xml.sax.SAXException;

public interface SaxEventBuffer {

  //  SAX Events
  static final int START_DOCUMENT         = 1;
  static final int END_DOCUMENT           = 2;
  static final int START_PREFIX_MAPPING   = 3;
  static final int END_PREFIX_MAPPING     = 4;
  static final int START_ELEMENT          = 5;
  static final int END_ELEMENT            = 6;
  static final int CHARACTERS             = 7;
  static final int IGNORABLE_WHITESPACE   = 8;
  static final int PROCESSING_INSTRUCTION = 9;
  static final int COMMENT                = 10;
  static final int LOCATOR                = 11;

  XMLReader createXmlReader();

  void replayEvents(ContentHandler handler) throws SAXException;

  void closeEvents(ContentHandler handler) throws SAXException;
}
