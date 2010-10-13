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

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ErrorHandler;

import com.servingxml.util.system.RuntimeContext;

public class DefaultSaxErrorHandler implements ErrorHandler {
  private final RuntimeContext context;

  public DefaultSaxErrorHandler(RuntimeContext context) {
    this.context = context;
  }

  public void warning (SAXParseException e) throws SAXException {
    context.warning(e.getMessage());
  }

  public void error(SAXParseException e) throws SAXException {
    throw e;
  }

  public void fatalError(SAXParseException e) throws SAXException {
    throw e;
  }
}

