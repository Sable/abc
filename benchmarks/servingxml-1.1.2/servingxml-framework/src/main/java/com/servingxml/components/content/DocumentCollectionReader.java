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
import com.servingxml.components.recordio.RecordPipeline;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.SystemConstants;
import com.servingxml.util.xml.AbstractXmlReader;
import com.servingxml.util.xml.FragmentContentHandler;
import com.servingxml.util.Name;

/**
 * A <code>DocumentCollectionReader</code> implement a SAX 2 
 * <code>XMLReader</code> interface for reading a sequence of 
 * XML documents. 
 * 
 * @author  Daniel A. Parker
 */

public class DocumentCollectionReader extends AbstractXmlReader {

  private final Name documentElementName;
  private final String documentElementQname;
  private final ServiceContext context;
  private final Flow flow;
  private final Content[] contentFactories;

  public DocumentCollectionReader(ServiceContext context, Flow flow,
                                Name documentElementName,
                                String documentElementQname,
                                Content[] contentFactories) {
    this.documentElementName = documentElementName;
    this.documentElementQname = documentElementQname;
    this.context = context;
    this.flow = flow;
    this.contentFactories = contentFactories;
  }

  public void parse(String systemId)
  throws IOException, SAXException {
    //System.out.println(record.getClass().getName() + ".parse start");

    final ContentHandler handler = getContentHandler();
    handler.startDocument();
    handler.startElement(documentElementName.getNamespaceUri(),documentElementName.getLocalName(),
                         documentElementQname,SystemConstants.EMPTY_ATTRIBUTES);

    ContentHandler sequenceHandler = new FragmentContentHandler(handler);
    for (int i = 0; i < contentFactories.length; ++i) {
      Content contentFactory = contentFactories[i];
      SaxSource saxSource = contentFactory.createSaxSource(context,flow);
      XMLReader reader = saxSource.createXmlReader();
      reader.setContentHandler(sequenceHandler);
      reader.parse(saxSource.getSystemId());
    }

    handler.endElement(documentElementName.getNamespaceUri(),documentElementName.getLocalName(),
                         documentElementQname);
    handler.endDocument();
  }
}

