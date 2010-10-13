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

package com.servingxml.components.inverserecordmapping;

import org.xml.sax.SAXException;

import com.servingxml.app.ServiceContext;
import com.servingxml.util.ServingXmlException;
import com.servingxml.expr.saxpath.SaxPath;
import com.servingxml.app.Flow;
import com.servingxml.components.recordio.RecordWriter;
import com.servingxml.app.ParameterDescriptor;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ShredXmlPrefilter implements ShredXml {
  private final ShredXml flattener;
  private final ParameterDescriptor[] parameterDescriptors;

  public ShredXmlPrefilter(ShredXml flattener,
    ParameterDescriptor[] parameterDescriptors) {

    this.flattener = flattener;
    this.parameterDescriptors = parameterDescriptors;
  }

  public boolean isMatched() {
    return flattener.isMatched();
  }

  public void matchPath(ServiceContext context, Flow flow, SaxPath path) 
  throws SAXException {
    try {
      Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
      flattener.matchPath(context, newFlow, path);
    } catch (ServingXmlException e) {
      throw new SAXException(e.getMessage(),e);
    }
  }

  public void startElement(ServiceContext context, Flow flow, SaxPath path,
    RecordWriter recordWriter) 
  throws SAXException {
    try {
      Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
      flattener.startElement(context, newFlow, path, recordWriter);
    } catch (ServingXmlException e) {
      throw new SAXException(e.getMessage(),e);
    }
  }
  public void characters(char ch[], int start, int length) throws SAXException {
    flattener.characters(ch,start,length);
  }
  public void endElement(ServiceContext context, Flow flow,
    String namespaceUri, String localName, String qname, RecordWriter recordWriter)
  throws SAXException {
    try {
      Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
      flattener.endElement(context, newFlow, namespaceUri, localName, qname, recordWriter);
    } catch (ServingXmlException e) {
      throw new SAXException(e.getMessage(),e);
    }
  }
  public void mapRecord(ServiceContext context, Flow flow, RecordWriter recordWriter)
  throws SAXException {
    try {
      Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
      flattener.mapRecord(context, newFlow, recordWriter);
    } catch (ServingXmlException e) {
      throw new SAXException(e.getMessage(),e);
    }
  }
}

