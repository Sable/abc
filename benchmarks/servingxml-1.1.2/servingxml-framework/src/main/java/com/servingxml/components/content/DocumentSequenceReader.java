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

package com.servingxml.components.content;

import java.io.IOException;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.recordio.AbstractRecordFilter;
import com.servingxml.components.recordio.RecordFilter;
import com.servingxml.components.recordio.RecordReaderFactory;
import com.servingxml.components.recordio.RecordPipeline;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.SystemConstants;
import com.servingxml.util.xml.AbstractXmlReader;
import com.servingxml.util.xml.FragmentContentHandler;
import com.servingxml.util.Name;

/**
 * A <code>DocumentSequenceReader</code> implement a SAX 2 
 * <code>XMLReader</code> interface for reading a sequence of 
 * XML documents. 
 * 
 * @author  Daniel A. Parker
 */

public class DocumentSequenceReader extends AbstractXmlReader {

  private final Name documentElementName;
  private final String documentElementQname;
  private final ServiceContext context;
  private final Flow flow;
  private final RecordReaderFactory recordReaderFactory;
  private final Content contentFactory;

  public DocumentSequenceReader(ServiceContext context, Flow flow,
                                Name documentElementName,
                                String documentElementQname,
                                RecordReaderFactory recordReaderFactory, 
                                Content contentFactory) {
    this.documentElementName = documentElementName;
    this.documentElementQname = documentElementQname;
    this.context = context;
    this.flow = flow;
    this.recordReaderFactory = recordReaderFactory;
    this.contentFactory = contentFactory;
  }

  public void parse(String systemId)
  throws IOException, SAXException {
    //System.out.println(record.getClass().getName() + ".parse start");

    final ContentHandler handler = getContentHandler();
    handler.startDocument();
    if (!documentElementName.isEmpty()) {
      handler.startElement(documentElementName.getNamespaceUri(),documentElementName.getLocalName(),
                           documentElementQname,SystemConstants.EMPTY_ATTRIBUTES);
    }

    RecordPipeline pipeline = recordReaderFactory.createRecordPipeline(context, flow);
    RecordFilter filter = new DocumentSequenceRecordFilter(contentFactory,handler);
    pipeline.addRecordFilter(filter);
    pipeline.execute(context);

    if (!documentElementName.isEmpty()) {
      handler.endElement(documentElementName.getNamespaceUri(),documentElementName.getLocalName(),
                           documentElementQname);
    }
    handler.endDocument();
  }
}

class DocumentSequenceRecordFilter extends AbstractRecordFilter {
  private final Content contentFactory;
  private final ContentHandler handler;

  public DocumentSequenceRecordFilter(Content contentFactory, ContentHandler handler) {
    this.contentFactory = contentFactory;
    this.handler = handler;
  }

  public void writeRecord(ServiceContext context, Flow flow) {
    try {
      SaxSource saxSource = contentFactory.createSaxSource(context,flow);
      XMLReader reader = saxSource.createXmlReader();
      ContentHandler sequenceHandler = new FragmentContentHandler(handler);
      reader.setContentHandler(sequenceHandler);
      reader.parse(saxSource.getSystemId());
    } catch (IOException e) {
      throw new ServingXmlException(e.getMessage(),e);
    } catch (SAXException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }
}

