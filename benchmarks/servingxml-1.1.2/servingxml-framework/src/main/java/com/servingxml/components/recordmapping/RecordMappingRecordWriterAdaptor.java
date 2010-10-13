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

import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;

import com.servingxml.app.Flow;
import com.servingxml.util.ServingXmlException;
import com.servingxml.app.ServiceContext;
import com.servingxml.util.record.Record; 
import com.servingxml.components.recordio.RecordWriter;
import com.servingxml.components.recordio.AbstractRecordWriter;
import com.servingxml.util.xml.PrefixMappingXmlFilter;
import com.servingxml.util.xml.ExtendedContentHandler;

/**
 * A <code>RecordMappingRecordWriterAdaptor</code> implements a <code>RecordWriter</code> interface for transforming 
 * flat file record values into SAX events.
 *
 * 
 * @author  Daniel A. Parker   
 */

public class RecordMappingRecordWriterAdaptor extends AbstractRecordWriter implements RecordWriter {

  private final MapXml toXmlBinding;
  private final ExtendedContentHandler extendedHandler;
  private final ContentHandler contentHandler;

  private Flow previousFlow = null;
  private Flow currentFlow = null;
  private Flow nextFlow = null;
  private boolean started = false;
  Record variables = Record.EMPTY;

  public RecordMappingRecordWriterAdaptor(MapXml toXmlBinding, ContentHandler handler) {
    this.contentHandler = handler;
    this.toXmlBinding = toXmlBinding;
    if (handler instanceof LexicalHandler) {
      this.extendedHandler = new PrefixMappingXmlFilter(handler,(LexicalHandler)handler);  
    } else {
      this.extendedHandler = new PrefixMappingXmlFilter(handler);  
    }
  }

  public void startRecordStream(ServiceContext context, Flow flow) {

    try {
      contentHandler.startDocument();
    } catch (SAXException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
    previousFlow = null;
    currentFlow = null;
    nextFlow = null;
    started = false;
  }

  public void writeRecord(ServiceContext context, Flow flow) {

    currentFlow = nextFlow;
    nextFlow = flow;
    if (currentFlow != null) {
      if (!started) {
        toXmlBinding.groupStarted(context, currentFlow, 
          null, nextFlow.getRecord(), extendedHandler, variables);
        started = true;
      }
      if (previousFlow == null) {
        toXmlBinding.writeRecord(context, currentFlow, 
          null, nextFlow.getRecord(), extendedHandler, GroupState.DEFAULT);
      } else {
        toXmlBinding.writeRecord(context, currentFlow, 
          previousFlow.getRecord(), nextFlow.getRecord(), 
          extendedHandler, GroupState.DEFAULT);
      }
    }
    previousFlow = currentFlow;
  }

  public void endRecordStream(ServiceContext context, Flow flow) {
    if (nextFlow != null) {
        if (currentFlow != null) {
          toXmlBinding.writeRecord(context, nextFlow, 
            currentFlow.getRecord(), null, extendedHandler, GroupState.DEFAULT);
        } else {
          toXmlBinding.groupStarted(context, nextFlow, 
            null, null, extendedHandler, variables);
          toXmlBinding.writeRecord(context, nextFlow, 
            null, null, extendedHandler, GroupState.DEFAULT);
        }
    } else {
      toXmlBinding.groupStarted(context, flow, 
        null, null, extendedHandler, variables);
    }
    //DEBUG:  flow changed to nextFlow
    toXmlBinding.groupStopped(context, nextFlow, extendedHandler);
    try {
      contentHandler.endDocument();
    } catch (SAXException e) {
      throw new ServingXmlException(e.getMessage(), e);
    }
  }

  public void close() {
  }
}

