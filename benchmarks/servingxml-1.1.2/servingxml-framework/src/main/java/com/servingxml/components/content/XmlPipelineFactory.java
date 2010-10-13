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

import java.util.Properties;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.content.Content;
import com.servingxml.app.xmlpipeline.XmlPipeline;

/**
 * Implements a pipeline factory
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class XmlPipelineFactory {
  private final Content[] xmlComponents;  

  public XmlPipelineFactory(Content[] xmlComponents) {
    this.xmlComponents = xmlComponents;
  }

  public XmlPipeline createPipeline(ServiceContext context, Flow flow,
                                    Properties defaultOutputProperties) {

    XmlPipeline pipeline = xmlComponents[0].createXmlPipeline(context,flow);
    for (int i = 1; i < xmlComponents.length; ++i) {
      Content xmlFilterAppender = xmlComponents[i];
      xmlFilterAppender.appendToXmlPipeline(context, flow, pipeline);
    }

    return pipeline;
  }
}
