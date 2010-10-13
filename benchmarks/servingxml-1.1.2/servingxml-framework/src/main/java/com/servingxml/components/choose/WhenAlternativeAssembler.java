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
import com.servingxml.components.xsltconfig.XsltConfiguration;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.components.recordmapping.MapXmlFactory;
import com.servingxml.components.recordmapping.MultipleMapXmlFactory;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.string.StringFactory;
import com.servingxml.components.string.StringFactoryCompiler;

/**
 * Assembler for assembling an <code>Alternative</code>.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 * @see Alternative
 */                          

public class WhenAlternativeAssembler {

  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private XsltConfiguration xsltConfiguration;
  private String test = "";
  private Task[] tasks = new Task[0];
  private Content[] xmlComponents = new Content[0];
  private RecordPipelineAppender[] recordPipelineAppenders = new RecordPipelineAppender[0];
  private MapXmlFactory[] recordMapFactories = new MapXmlFactory[0];

  public void setTest(String test) {
    this.test = test;
  }

  public void injectComponent(XsltConfiguration xsltConfiguration) {
    this.xsltConfiguration = xsltConfiguration;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {
    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(Task[] tasks) {
    this.tasks = tasks;
  }

  public void injectComponent(MapXmlFactory[] recordMapFactories) {
    this.recordMapFactories = recordMapFactories;
  }

  public void injectComponent(Content[] xmlComponents) {
    this.xmlComponents = xmlComponents;
  }

  public void injectComponent(RecordPipelineAppender[] recordPipelineAppenders) {
    this.recordPipelineAppenders = recordPipelineAppenders;
  }

  public Alternative assemble(final ConfigurationContext context) {

    if (test.length() == 0) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,context.getElement().getTagName(),
                                                                 "test");
      throw new ServingXmlException(message);
    }
    StringFactory stringFactory = StringFactoryCompiler.fromStringables(context,context.getElement());

    try {
      if (xsltConfiguration == null) {
        xsltConfiguration = XsltConfiguration.getDefault();
      }

      MapXmlFactory rmf = recordMapFactories.length > 0 ?
        new MultipleMapXmlFactory(context.getQnameContext(), xsltConfiguration, recordMapFactories) : null;

      Alternative choice = new WhenAlternative(parameterDescriptors, 
                                               test, 
                                               tasks, 
                                               xmlComponents,
                                               recordPipelineAppenders, 
                                               stringFactory, 
                                               rmf);

      return choice;
    } catch (ServingXmlException e) {
      throw e.contextualizeMessage(context.getElement().getTagName());
    } catch (Exception e) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ERROR,
        context.getElement().getTagName(), e.getMessage());
      throw new ServingXmlException(message,e);
    }
  }

}




