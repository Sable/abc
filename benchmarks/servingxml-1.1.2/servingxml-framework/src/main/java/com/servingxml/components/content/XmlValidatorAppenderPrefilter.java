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

import java.util.List;

import com.servingxml.app.Flow;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.xmlpipeline.XmlFilterChain;
import com.servingxml.app.xmlpipeline.XmlPipeline;
import com.servingxml.app.xmlpipeline.XmlFilterChain;
import com.servingxml.components.common.Validator;
import com.servingxml.io.saxsink.SaxSink;
import com.servingxml.io.saxsource.SaxSource;

public class XmlValidatorAppenderPrefilter implements XmlValidatorAppender {
  private final XmlValidatorAppender validator;
  protected final ParameterDescriptor[] parameterDescriptors;

  public XmlValidatorAppenderPrefilter(XmlValidatorAppender validator,
                                    ParameterDescriptor[] parameterDescriptors) {
    this.validator = validator;
    this.parameterDescriptors = parameterDescriptors;
  }

  public void appendToXmlPipeline(ServiceContext context, Flow flow,
                                  XmlFilterChain pipeline) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    validator.appendToXmlPipeline(context, newFlow, pipeline);
  }

  public boolean validate(ServiceContext context, Flow flow, List<String> failures) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    return validator.validate(context, newFlow, failures);
  }

  public XmlPipeline createXmlPipeline(ServiceContext context, Flow flow) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    XmlPipeline pipeline = new XmlPipeline(/*defaultOutputProperties*/);
    SaxSource saxSource = flow.getDefaultSaxSource();
    pipeline.setSaxSource(saxSource);
    appendToXmlPipeline(context, newFlow, pipeline);
    return pipeline;
  }

  public SaxSource createSaxSource(ServiceContext context, Flow flow) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    SaxSource saxSource = newFlow.getDefaultSaxSource();
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
