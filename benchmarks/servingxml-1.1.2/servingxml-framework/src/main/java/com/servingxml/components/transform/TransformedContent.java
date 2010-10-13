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

package com.servingxml.components.transform;

import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;

//DEBUG
import com.servingxml.util.xml.ContentHandlerFilter;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import com.servingxml.io.saxsink.SaxSink;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.components.content.Content;
import com.servingxml.components.saxsink.SaxSinkFactory;
import com.servingxml.app.xmlpipeline.XmlPipeline;
import com.servingxml.app.xmlpipeline.XmlFilterChain;
import com.servingxml.components.property.OutputProperty;
import com.servingxml.components.property.OutputPropertyFactory;
import com.servingxml.io.saxsource.XmlReaderSaxSource;
import com.servingxml.components.content.AbstractContent;

/**
 * A filter component filters the stream of sax events.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)  
 */

public final class TransformedContent extends AbstractContent 
implements ContentTask {

  private final Content[] xmlComponents;
  private final SaxSinkFactory saxSinkFactory;

  public TransformedContent(Content[] xmlComponents,
                          SaxSinkFactory saxSinkFactory, 
                          OutputPropertyFactory[] defaultOutputPropertyFactories) {
    super(defaultOutputPropertyFactories);

    //System.out.println("TransformedContent cons Enter");

    this.xmlComponents = xmlComponents;
    this.saxSinkFactory = saxSinkFactory;
  }

  public void appendToXmlPipeline(ServiceContext context, Flow flow, XmlFilterChain pipeline) {
    for (int i = 0; i < xmlComponents.length; ++i) {
      Content xmlFilterAppender = xmlComponents[i];
     //System.out.println(getClass().getName()+".appendToXmlPipeline filter="+xmlFilterAppender.getClass().getName());
      xmlFilterAppender.appendToXmlPipeline(context,flow,pipeline);
    }
   //System.out.println("TransformedContent.appendToXmlPipeline leave");
  }

  public XmlPipeline createXmlPipeline(ServiceContext context, Flow flow) {
    XmlPipeline pipeline = xmlComponents[0].createXmlPipeline(context,flow);
    for (int i = 1; i < xmlComponents.length; ++i) {
      Content xmlFilterAppender = xmlComponents[i];
     //System.out.println(getClass().getName()+".appendToXmlPipeline filter="+xmlFilterAppender.getClass().getName());
      xmlFilterAppender.appendToXmlPipeline(context,flow,pipeline);
    }
    return pipeline;
  }

  public void execute(ServiceContext context, Flow flow) {

    SaxSink saxSink = null;
    //System.out.println(getClass().getName()+".execute enter saxSinkFactory=" + saxSinkFactory.getClass().getName());

    try {
      saxSink = saxSinkFactory.createSaxSink(context, flow);
      flow = flow.replaceDefaultSaxSink(context, saxSink);

      XmlPipeline pipeline = createXmlPipeline(context,flow);
      saxSink.setOutputProperties(pipeline.getOutputProperties());

     //System.out.println(getClass().getName()+".execute saxSink=" + saxSink.getClass().getName());
     //System.out.println(getClass().getName()+".execute contentHandler=" + saxSink.getContentHandler().getClass().getName());

      //DEBUG
      /*ContentHandler handler = new ContentHandlerFilter(saxSink.getContentHandler()) {
          public void startDocument() throws SAXException {
            //System.out.println(getClass().getName()+".startDocument");
            super.startDocument();
          }
          public void endDocument() throws SAXException {
            //System.out.println(getClass().getName()+".endDocument");
            super.endDocument();
          }
      };*/
      pipeline.execute(saxSink.getContentHandler());
    } finally {
      if (saxSink != null) {
        saxSink.close();
      }
    }
   //System.out.println(getClass().getName()+".execute leave");
  }

  public SaxSource createSaxSource(ServiceContext context, Flow flow) {
    XmlPipeline pipeline = createXmlPipeline(context,flow);
    SaxSource saxSource = new XmlReaderSaxSource(pipeline.getXmlReader(),
      pipeline.getOutputProperties(), context.getTransformerFactory());
    return saxSource;
  }
}

