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

package com.servingxml.components.saxfilter;

import com.servingxml.app.Flow;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.xmlpipeline.XmlPipeline;
import com.servingxml.app.xmlpipeline.XmlFilterChain;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.io.saxsink.SaxSink;
import com.servingxml.components.content.Content;

public class XmlFilterAppenderPrefilter implements Content {
  private final Content[] xmlComponents;
  private final ParameterDescriptor[] parameterDescriptors;

  public XmlFilterAppenderPrefilter(Content xmlFilterAppender,
  ParameterDescriptor[] parameterDescriptors) {
    this.xmlComponents = new Content[]{xmlFilterAppender};
    this.parameterDescriptors = parameterDescriptors;
  }

  public XmlFilterAppenderPrefilter(Content[] xmlComponents,
  ParameterDescriptor[] parameterDescriptors) {
    this.xmlComponents = xmlComponents;
    this.parameterDescriptors = parameterDescriptors;
  }

  public void appendToXmlPipeline(ServiceContext context, Flow flow,
                        XmlFilterChain pipeline) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    for (int i = 0; i < xmlComponents.length; ++i) {
      xmlComponents[i].appendToXmlPipeline(context, newFlow, pipeline);
    }
  }

  public XmlPipeline createXmlPipeline(ServiceContext context, Flow flow) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    XmlPipeline pipeline = xmlComponents[0].createXmlPipeline(context,newFlow);
    for (int i = 1; i < xmlComponents.length; ++i) {
      xmlComponents[i].appendToXmlPipeline(context,newFlow,pipeline);
    }
    return pipeline;
  }

  public SaxSource createSaxSource(ServiceContext context, Flow flow) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    SaxSource saxSource = xmlComponents[0].createSaxSource(context,newFlow);
    return saxSource;
  }

  public String createString(ServiceContext context, Flow flow) {
    return "";
  }

  public void execute(ServiceContext context, Flow flow) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);

    SaxSink saxSink = null;
    //System.out.println(getClass().getName()+".execute enter saxSinkFactory=" + saxSinkFactory.getClass().getName());

    try {
      saxSink = newFlow.getDefaultSaxSink();
      newFlow = newFlow.replaceDefaultSaxSink(context, saxSink);
      XmlPipeline pipeline = createXmlPipeline(context,newFlow);
      saxSink.setOutputProperties(pipeline.getOutputProperties());
      pipeline.execute(saxSink.getContentHandler());
    } finally {
      if (saxSink != null) {
        saxSink.close();
      }
    }
  }
}
