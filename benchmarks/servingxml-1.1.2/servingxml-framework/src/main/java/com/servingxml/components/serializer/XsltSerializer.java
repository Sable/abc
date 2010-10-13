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

import java.io.OutputStream;
import java.util.Properties;
import javax.xml.transform.sax.TransformerHandler;
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

public class XsltSerializer implements SaxSink {

  private final TransformerHandler handler;
  private final StreamSink streamSink;
  private final OutputProperty[] serializerOutputProperties;

  public XsltSerializer(TransformerHandler handler, StreamSink streamSink,
    OutputProperty[] serializerOutputProperties) {
    this.streamSink = streamSink;
    this.handler = handler;
    this.serializerOutputProperties = serializerOutputProperties;
  }

  public XsltSerializer(TransformerHandler handler, StreamSink streamSink) {
    this.streamSink = streamSink;
    this.handler = handler;
    this.serializerOutputProperties = new OutputProperty[0];
  }

  public ContentHandler getContentHandler() {
    //System.out.println(getClass().getName()+".getContentHandler");
    return handler;
  }

  public void close() {
    //System.out.println(getClass().getName()+".close");
    ServingXmlException badDispose = null;
    try {
      streamSink.close();
    } catch (ServingXmlException e) {
      badDispose = e;
    } catch (Exception e) {
      badDispose = new ServingXmlException(e.getMessage(),e);
    }
    if (badDispose != null) {
      throw badDispose;
    }
  }

  public void setOutputProperties(Properties outputProperties) {
    Properties properties = new Properties();
    Enumeration propEnum = outputProperties.propertyNames();
    while (propEnum.hasMoreElements()) {
      String name = (String)propEnum.nextElement();
      String value = outputProperties.getProperty(name);
      properties.setProperty(name, value);
    }

    if (serializerOutputProperties.length > 0) {
      for (int i = 0; i < serializerOutputProperties.length; ++i) {
        OutputProperty outputProperty = serializerOutputProperties[i];
        properties.setProperty(outputProperty.getName(),outputProperty.getValue());
      }
    }
    handler.getTransformer().setOutputProperties(properties);
  }
}

