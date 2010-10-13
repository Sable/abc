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

package com.servingxml.components.serializer;

import java.util.Properties;
import java.util.Enumeration;

import org.xml.sax.ContentHandler;

import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.util.ServingXmlException;
import com.servingxml.io.saxsink.SaxSink;
import com.servingxml.components.property.OutputProperty;

/**
 * Writes the transformed XML directly to an output stream with no additional formatting.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */             

public class BatchedSerializer implements SaxSink {

  private final BatchedContentHandler handler;

  public BatchedSerializer(BatchedContentHandler handler) {
    this.handler = handler;
  }

  public ContentHandler getContentHandler() {
    //System.out.println(getClass().getName()+".getContentHandler");
    return handler;
  }

  public void close() {
  }

  public void setOutputProperties(Properties outputProperties) {
    handler.setOutputProperties(outputProperties);
  }
}

