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

import com.servingxml.components.recordio.RecordPipeline;
import com.servingxml.components.recordio.RecordWriter;
import com.servingxml.app.ServiceContext;
import com.servingxml.util.xml.AbstractXmlReader;
import com.servingxml.util.ServingXmlException;
import com.servingxml.components.recordio.RecordWriterFilterAdaptor;

/**
 * A <code>RecordStreamXmlReader</code> implements a <code>RecordWriter</code> interface for transforming 
 * flat file record values into SAX events.
 *
 * 
 * @author  Daniel A. Parker
 */

public class RecordStreamXmlReader extends AbstractXmlReader {

  private final ServiceContext context;
  private final RecordPipeline recordPipeline;
  private final MapXml onRecordMapping;

  public RecordStreamXmlReader(ServiceContext context,
  RecordPipeline recordPipeline, MapXml onRecordMapping) {
    this.context = context;
    this.recordPipeline = recordPipeline;
    this.onRecordMapping = onRecordMapping;
  }

  public void parse(String systemId)
  throws IOException, SAXException {                         

    try {
      RecordWriter recordWriter = new RecordMappingRecordWriterAdaptor(onRecordMapping, getContentHandler());
      //recordPipeline.setRecordWriter(recordWriter);
      recordPipeline.addRecordFilter(new RecordWriterFilterAdaptor(recordWriter));
      recordPipeline.execute(context);
    } catch (ServingXmlException e) {
      throw new SAXException(e.getMessage(), e);
    }
  }
}

