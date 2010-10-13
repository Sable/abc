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

package com.servingxml.components.choose;

import com.servingxml.components.recordio.RecordPipelineAppender;
import com.servingxml.components.task.Task;
import com.servingxml.components.content.Content;
import com.servingxml.components.recordmapping.MapXmlFactory;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;
import com.servingxml.app.xmlpipeline.XmlFilterChain;
import com.servingxml.app.xmlpipeline.XmlPipeline;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.components.string.StringFactory;
import com.servingxml.util.Name;
import com.servingxml.util.record.Record;   
import com.servingxml.io.saxsource.XmlReaderSaxSource;

/**
 * Provides a default implementation for most methods of an <code>Alternative</code>.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 * @see Alternative
 */

abstract class AbstractAlternative 
implements Alternative {

  private final ParameterDescriptor[] parameterDescriptors;
  private final Task[] tasks;
  private final Content[] xmlComponents;
  private final RecordPipelineAppender[] recordPipelineAppenders;
  private final MapXmlFactory recordMapFactory;
  private final StringFactory stringFactory;

  public AbstractAlternative(ParameterDescriptor[] parameterDescriptors,
                             Task[] tasks, 
                             Content[] xmlComponents,
                             RecordPipelineAppender[] recordPipelineAppenders,
                             StringFactory stringFactory,
                             MapXmlFactory recordMapFactory) {

    this.parameterDescriptors = parameterDescriptors;
    this.tasks = tasks;
    this.xmlComponents = xmlComponents;
    this.recordPipelineAppenders = recordPipelineAppenders;
    this.stringFactory = stringFactory;
    this.recordMapFactory = recordMapFactory;
  }

  public RecordPipelineAppender[] getRecordPipelineAppenders() {
    return recordPipelineAppenders;
  }

  public Content[] getXmlComponents() {
    return xmlComponents;
  }

  public MapXmlFactory getRecordMapFactory() {
    return recordMapFactory;
  }

  public void execute(ServiceContext context, Flow flow) {
    //System.out.println(getClass().getName()+".execute enter");

    flow = flow.augmentParameters(context,parameterDescriptors);

    for (int j = 0; j < tasks.length; ++j) {
      Task task = tasks[j];
      task.execute(context, flow);
    }
  }

  public void appendToXmlPipeline(ServiceContext context, Flow flow, XmlFilterChain pipeline) {
    //System.out.println(getClass().getName()+".appendToXmlPipeline enter");

    flow = flow.augmentParameters(context,parameterDescriptors);

    for (int j = 0; j < xmlComponents.length; ++j) {
      Content xmlFilterAppender = xmlComponents[j];
      //System.out.println(getClass().getName()+".appendToXmlPipeline filter="+xmlFilterAppender.getClass().getName());
      xmlFilterAppender.appendToXmlPipeline(context, flow, pipeline);
    }
  }

  public SaxSource createSaxSource(ServiceContext context, Flow flow) {
    XmlPipeline pipeline = createXmlPipeline(context,flow);
    SaxSource saxSource = new XmlReaderSaxSource(pipeline.getXmlReader(),
      pipeline.getOutputProperties(), context.getTransformerFactory());
    return saxSource;
  }

  public XmlPipeline createXmlPipeline(ServiceContext context, Flow flow) {
    XmlPipeline pipeline = new XmlPipeline();
    if (xmlComponents.length > 0) {
      pipeline = xmlComponents[0].createXmlPipeline(context,flow);
      for (int j = 1; j < xmlComponents.length; ++j) {
        Content xmlFilterAppender = xmlComponents[j];
        //System.out.println(getClass().getName()+".appendToXmlPipeline filter="+xmlFilterAppender.getClass().getName());
        xmlFilterAppender.appendToXmlPipeline(context, flow, pipeline);
      }
    }
    return pipeline;
  }

  public String createString(ServiceContext context, Flow flow) {
    String s = null;
    if (stringFactory != null) {
      s = stringFactory.createString(context,flow);
    } else if (tasks.length > 0) {
      s = tasks[0].createString(context,flow);
    }
    return s;
  }
}



