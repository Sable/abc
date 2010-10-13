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

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Properties;
import java.util.Enumeration;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.SAXException;

import com.servingxml.app.ServiceContext;
import com.servingxml.components.streamsink.StreamSinkFactory;
import com.servingxml.components.property.OutputProperty;
import com.servingxml.components.property.OutputPropertyFactory;
import com.servingxml.app.Flow;
import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.util.ServingXmlException;
import com.servingxml.io.saxsink.SaxSink;
import com.servingxml.components.saxsink.SaxSinkFactory;

/**
 * 
 * @author  Daniel A. Parker
 */

public class XsltSerializerFactory implements SaxSinkFactory {
  private final StreamSinkFactory sinkFactory;
  private final OutputPropertyFactory[] outputPropertyFactories;

  public XsltSerializerFactory(StreamSinkFactory sinkFactory) {
    this.sinkFactory = sinkFactory;
    this.outputPropertyFactories = new OutputPropertyFactory[0];
  }

  public XsltSerializerFactory(StreamSinkFactory sinkFactory, OutputPropertyFactory[] outputPropertyFactories) {
    this.sinkFactory = sinkFactory;
    this.outputPropertyFactories = outputPropertyFactories;
  }

  public SaxSink createSaxSink(ServiceContext context, Flow flow) {

    //System.out.println(getClass().getName()+".createSerializer 1 indent="+defaultOutputProperties.getProperty("indent")); 

    /*
    Properties properties = new Properties();
    Enumeration propEnum = defaultOutputProperties.propertyNames();
    while (propEnum.hasMoreElements()) {
      String name = (String)propEnum.nextElement();
      String value = defaultOutputProperties.getProperty(name);
      properties.setProperty(name, value);
    }

    if (outputProperties.length > 0) {
      properties = new Properties(defaultOutputProperties);
      for (int i = 0; i < outputProperties.length; ++i) {
        OutputProperty outputProperty = outputProperties[i];
        properties.setProperty(outputProperty.getName(),outputProperty.getValue());
      }
    }
    */
    //System.out.println(getClass().getName()+".createSerializer 2 indent="+ properties.getProperty("indent") + " " + properties.get("indent")); 

    StreamSink streamSink;
    try {
      streamSink = sinkFactory.createStreamSink(context, flow);

      SAXTransformerFactory saxFactory = context.getTransformerFactory();

      //  TODO:  initialize
      TransformerHandler handler = saxFactory.newTransformerHandler();

      Result result = streamSink.getResult();
      handler.setResult(result);
      //Transformer transformer = handler.getTransformer();

      //streamSink.setOutputProperties(properties);
      //transformer.setOutputProperties(properties);

      OutputProperty[] outputProperties = new OutputProperty[outputPropertyFactories.length];
      for (int i = 0; i < outputPropertyFactories.length; ++i) {
        outputProperties[i] = outputPropertyFactories[i].createOutputProperty(context,flow);
      }

      return new XsltSerializer(handler, streamSink, outputProperties);
    } catch (TransformerException te) {
      Throwable cause = te;
      if (te.getCause() != null) {
        cause = te.getCause();
        if (cause instanceof SAXException) {
          SAXException se = (SAXException)cause;
          if (se != null && se.getException() != null && se.getException().getMessage() != null) {
            cause = se.getException();
          }
        }
      }
      if (cause instanceof ServingXmlException) {
        throw (ServingXmlException)cause;
      } else {
        throw new ServingXmlException(cause.getMessage(),cause);
      }
    }
  }
}
