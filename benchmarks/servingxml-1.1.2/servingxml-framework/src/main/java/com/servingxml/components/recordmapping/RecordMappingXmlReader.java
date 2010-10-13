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

package com.servingxml.components.recordmapping;

import java.io.IOException;

import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;

import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;
import com.servingxml.util.xml.AbstractXmlReader;
import com.servingxml.util.xml.ExtendedContentHandler;
import com.servingxml.util.xml.PrefixMappingXmlFilter;

/**
 * A <code>RecordMappingXmlReader</code> implements a <code>RecordWriter</code> interface for transforming 
 * flat file record values into SAX events.
 *
 * 
 * @author  Daniel A. Parker   
 */

public class RecordMappingXmlReader extends AbstractXmlReader {

  private final ServiceContext context;
  private final Flow flow;
  private final MapXml recordMap;

  public RecordMappingXmlReader(ServiceContext context, Flow flow, MapXml recordMap) {
    this.context = context;
    this.flow = flow;
    this.recordMap = recordMap;
  }

  public void parse(String systemId)
  throws IOException, SAXException {
    try {
      ContentHandler handler = getContentHandler();
      ExtendedContentHandler extendedHandler = null;

      if (handler instanceof LexicalHandler) {
        extendedHandler = new PrefixMappingXmlFilter(handler,(LexicalHandler)handler);  
      } else {
        extendedHandler = new PrefixMappingXmlFilter(handler);  
      }

      handler.startDocument();
      recordMap.groupStarted(context, flow, Record.EMPTY, Record.EMPTY, extendedHandler, Record.EMPTY);
      recordMap.writeRecord(context,flow,Record.EMPTY,Record.EMPTY, extendedHandler, GroupState.DEFAULT);
      recordMap.groupStopped(context, flow, extendedHandler);
      handler.endDocument();
    } catch (ServingXmlException e) {
      throw new SAXException(e.getMessage(), e);
    }
  }
}

