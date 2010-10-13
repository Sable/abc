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

import java.util.Properties;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.xmlpipeline.XmlFilterChain;
import com.servingxml.app.xmlpipeline.XmlPipeline;
import com.servingxml.components.content.XmlPipelineFactory;
import com.servingxml.components.content.Content;
import com.servingxml.components.property.OutputProperty;
import com.servingxml.components.property.OutputPropertyFactory;
import com.servingxml.components.recordio.AbstractRecordReaderFactory;
import com.servingxml.components.recordio.RecordFilterAppender;
import com.servingxml.components.recordio.RecordReader;
import com.servingxml.components.recordio.RecordReaderFactory;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.io.saxsink.SaxSink;

/**
 * Implements an interface for a RecordFilterAppender and a 
 * Content. 
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */                            

public class SubtreeRecordReaderFactory extends AbstractRecordReaderFactory 
  implements RecordReaderFactory, RecordFilterAppender, Content {
  private final OutputPropertyFactory[] defaultOutputPropertyFactories;
  private final InverseRecordMapping inverseRecordMapping;
  private final Content[] xmlComponents;

  public SubtreeRecordReaderFactory(Content[] xmlComponents,
    OutputPropertyFactory[] defaultOutputPropertyFactories,
    InverseRecordMapping inverseRecordMapping) {
    this.xmlComponents = xmlComponents;
    this.defaultOutputPropertyFactories = defaultOutputPropertyFactories;
    this.inverseRecordMapping = inverseRecordMapping;
  }                                     

  public void appendToXmlPipeline(ServiceContext context, Flow flow,
    XmlFilterChain pipeline) {

    for (int i = 0; i < xmlComponents.length; ++i) {
      Content xmlFilterAppender = xmlComponents[i];
      xmlFilterAppender.appendToXmlPipeline(context, flow, pipeline);
    }
  }

  protected RecordReader createRecordReader(ServiceContext context, Flow flow) {
    XmlPipelineFactory pipelineFactory = new XmlPipelineFactory(xmlComponents);

    Properties defaultOutputProperties = new Properties();
    for (int i = 0; i < defaultOutputPropertyFactories.length; ++i) {
      OutputProperty property = defaultOutputPropertyFactories[i].createOutputProperty(context,flow);
      defaultOutputProperties.setProperty(property.getName(), property.getValue());
    }

    RecordReader recordReader = new SubtreeRecordReader(pipelineFactory, defaultOutputProperties, inverseRecordMapping);
    return recordReader;
  }

  public SaxSource createSaxSource(ServiceContext context, Flow flow) {
    SaxSource saxSource = flow.getDefaultSaxSource();
    return saxSource;
  }

  public XmlPipeline createXmlPipeline(ServiceContext context, Flow flow) {
    XmlPipeline pipeline = new XmlPipeline(/*defaultOutputProperties*/);
    SaxSource saxSource = flow.getDefaultSaxSource();
    pipeline.setSaxSource(saxSource);
    appendToXmlPipeline(context, flow, pipeline);
    return pipeline;
  }

  public String createString(ServiceContext context, Flow flow) {
    return "";
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
}
