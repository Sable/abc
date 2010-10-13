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

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.xmlpipeline.XmlPipeline;
import com.servingxml.components.property.OutputProperty;
import com.servingxml.components.property.OutputPropertyFactory;
import com.servingxml.components.saxsink.NullSaxSinkFactory;
import com.servingxml.components.saxsink.SaxSinkFactory;
import com.servingxml.components.serializer.XsltSerializer;
import com.servingxml.components.task.AbstractTask;
import com.servingxml.io.saxsink.SaxSink;
import com.servingxml.io.streamsink.OutputStreamSinkAdaptor;
import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.io.streamsink.StringStreamSink;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;
import com.servingxml.util.xml.ContentHandlerFilter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;
import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

public abstract class AbstractContent implements Content {

  private final OutputPropertyFactory[] defaultOutputPropertyFactories;

  public AbstractContent() {         
    this.defaultOutputPropertyFactories = new OutputPropertyFactory[0];
  }

  public AbstractContent(OutputPropertyFactory[] defaultOutputPropertyFactories) {         
    this.defaultOutputPropertyFactories = defaultOutputPropertyFactories;
  }
/*
  public String createString(ServiceContext context, Flow flow) {
    //  Create serializer, output stream is string stream
    //  Create pipeline, execute to content handler of serializer

    //System.out.println(getClass().getName()+".createString enter");
    SaxSink saxSink = null;
    try {
      StringStreamSink streamSink = new StringStreamSink();
      Result result = streamSink.getResult();
      SAXTransformerFactory handlerFactory = context.getTransformerFactory();
      TransformerHandler handler = handlerFactory.newTransformerHandler();
      handler.setResult(result);
      saxSink = new XsltSerializer(handler, streamSink);
      flow = flow.replaceDefaultSaxSink(context, saxSink);
      XmlPipeline pipeline = createXmlPipeline(context,flow);
      saxSink.setOutputProperties(pipeline.getOutputProperties());
      pipeline.execute(saxSink.getContentHandler());
          //pipeline.execute(h);

      String s = streamSink.toString();
      //System.out.println(getClass().getName()+".createString " + s +".");
      return s;
    } catch (ServingXmlException e) {
      throw e;
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(),e);
    } finally {
      if (saxSink != null) {
        saxSink.close();
      }
    }
  }
*/

  public String createString(ServiceContext context, Flow flow) {
    //  Create serializer, output stream is string stream
    //  Create pipeline, execute to content handler of serializer

    //System.out.println(getClass().getName()+".createString enter");
    SaxSink saxSink = null;
    try {
      StringStreamSink streamSink = new StringStreamSink();
      Result result = streamSink.getResult();
      SAXTransformerFactory handlerFactory = context.getTransformerFactory();
      TransformerHandler handler = handlerFactory.newTransformerHandler();
      handler.setResult(result);
      saxSink = new XsltSerializer(handler, streamSink);
      flow = flow.replaceDefaultSaxSink(context, saxSink);
      flow = flow.replaceDefaultStreamSink(context, streamSink);
      execute(context,flow);

      String s = streamSink.toString();
      //System.out.println(getClass().getName()+".createString " + s +".");
      return s;
    } catch (ServingXmlException e) {
      throw e;
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(),e);
    } finally {
      if (saxSink != null) {
        saxSink.close();
      }
    }
  }

  public void execute(ServiceContext context, Flow flow) {

    SaxSink saxSink = null;
    //System.out.println(getClass().getName()+".execute enter saxSinkFactory=" + saxSinkFactory.getClass().getName());

    try {
      saxSink = flow.getDefaultSaxSink();
      flow = flow.replaceDefaultSaxSink(context, saxSink);
      XmlPipeline pipeline = createXmlPipeline(context,flow);
      saxSink.setOutputProperties(pipeline.getOutputProperties());
      pipeline.execute(saxSink.getContentHandler());
    } finally {
      if (saxSink != null) {
        saxSink.close();
      }
    }
  }

  public Properties createDefaultOutputProperties(ServiceContext context, Flow flow) {
    Properties properties = new Properties();
    for (int i = 0; i < defaultOutputPropertyFactories.length; ++i) {
      OutputProperty property = defaultOutputPropertyFactories[i].createOutputProperty(context,flow);
      properties.setProperty(property.getName(), property.getValue());
    }
    return properties;
  }
}
