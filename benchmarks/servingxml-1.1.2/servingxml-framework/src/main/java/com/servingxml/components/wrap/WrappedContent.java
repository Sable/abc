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

package com.servingxml.components.wrap;

import java.util.Properties;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.xmlpipeline.XmlFilterChain;
import com.servingxml.app.xmlpipeline.XmlPipeline;
import com.servingxml.components.content.AbstractContent;
import com.servingxml.components.content.Content;
import com.servingxml.components.property.OutputProperty;
import com.servingxml.components.property.OutputPropertyFactory;
import com.servingxml.components.saxsink.SaxSinkFactory;
import com.servingxml.components.transform.ContentTask;
import com.servingxml.io.saxsink.SaxSink;
import com.servingxml.io.saxsink.SimpleSaxSink;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.io.saxsource.XmlReaderSaxSource;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.xml.ChainedContentHandler;
import com.servingxml.util.xml.FragmentContentHandler;
import com.servingxml.util.xml.TerminalXmlFilter;

/**
 * A filter component filters the stream of sax events.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)  
 */

public final class WrappedContent extends AbstractContent 
  implements ContentTask {

  private final Content content;
  private final SaxSinkFactory saxSinkFactory;

  public WrappedContent(Content content,
    SaxSinkFactory saxSinkFactory, OutputPropertyFactory[] defaultOutputPropertyFactories) {
    super(defaultOutputPropertyFactories);

    //System.out.println("WrappedContent cons Enter");

    this.content = content;
    this.saxSinkFactory = saxSinkFactory;

  }

  public void appendToXmlPipeline(ServiceContext context, Flow flow, XmlFilterChain pipeline) {
    content.appendToXmlPipeline(context,flow,pipeline);
   //System.out.println("TransformedContent.appendToXmlPipeline leave");
  }

  public XmlPipeline createXmlPipeline(ServiceContext context, Flow flow) {
    XmlPipeline pipeline = content.createXmlPipeline(context,flow);
    return pipeline;
   //System.out.println("TransformedContent.appendToXmlPipeline leave");
  }

  public void execute(ServiceContext context, Flow flow) {
    //System.out.println(getClass().getName()+".execute enter");

    SaxSink saxSink = null;
    try {
      //System.out.println(getClass().getName()+".execute "  + defaultOutputProperties.getProperty("indent"));
      saxSink = saxSinkFactory.createSaxSink(context, flow);

      XmlPipeline pipeline = createXmlPipeline(context,flow);
      saxSink.setOutputProperties(pipeline.getOutputProperties());

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
      ContentHandler contentHandler = saxSink.getContentHandler();
/*
      contentHandler = new ChainedContentHandler(contentHandler) {
          public void startDocument() throws SAXException {
            //System.out.println("###startDocument");
            super.startDocument();
          }
          public void endDocument() throws SAXException {
            //System.out.println("###endDocument");
            super.endDocument();
          }

          public void startElement(String namespaceUri, String localName, String qname, 
            Attributes atts) throws SAXException {
            //System.out.println("###startElement " + qname);
            super.startElement(namespaceUri,localName,qname,atts);
          }

          public void endElement(String namespaceUri, String localName, String qname) throws SAXException {
            //System.out.println("###endElement " + qname);
            super.endElement(namespaceUri,localName,qname);
          }
      };
*/
      //System.out.println(getClass().getName()+".execute startDocument");
      contentHandler.startDocument();
      pipeline.execute(new WrapFilter(new FragmentContentHandler(contentHandler)));
      contentHandler.endDocument();
      //System.out.println(getClass().getName()+".execute endDocument");
    } catch (SAXException e) {
      throw new ServingXmlException(e.getMessage(),e);
    } finally {
      if (saxSink != null) {
        saxSink.close();
      }
    }
  }

  public SaxSource createSaxSource(ServiceContext context, Flow flow) {
   //System.out.println(getClass().getName()+".createSaxSource enter");
    XmlPipeline pipeline = createXmlPipeline(context,flow);
    SaxSource saxSource = new XmlReaderSaxSource(pipeline.getXmlReader(),
      pipeline.getOutputProperties(), context.getTransformerFactory());
    // Add pipeline properties to saxSource
   //System.out.println(getClass().getName()+".createSaxSource leave");
    return saxSource;
  }
}

