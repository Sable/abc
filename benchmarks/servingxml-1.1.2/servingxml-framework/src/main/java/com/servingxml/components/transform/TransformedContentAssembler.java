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

import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.content.Content;
import com.servingxml.components.content.ContentPrefilter;
import com.servingxml.components.content.DefaultDocument;
import com.servingxml.components.saxsink.SaxSinkFactory;
import com.servingxml.components.xsltconfig.XsltConfiguration;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.components.error.CatchError;

/**
 * Factory for creating a pipeline component.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 * @see Content
 */               

public class TransformedContentAssembler {

  private Content[] xmlComponents = new Content[0];
  private SaxSinkFactory saxSinkFactory = SaxSinkFactory.DEFAULT;
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private XsltConfiguration xsltConfiguration = null;
  private CatchError catchError = null;

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {
    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(Content[] xmlComponents) {
    this.xmlComponents = xmlComponents;
  }

  public void injectComponent(SaxSinkFactory saxSinkFactory) {
    this.saxSinkFactory = saxSinkFactory;
  }

  public void injectComponent(XsltConfiguration xsltConfiguration) {
    this.xsltConfiguration = xsltConfiguration;
  }

  public ContentTask assemble(ConfigurationContext context) {
    //System.out.println("TransformedContentAssembler.assemble Enter");
    try {
      if (xsltConfiguration == null) {
        xsltConfiguration = XsltConfiguration.getDefault();
      }

      if (xmlComponents.length == 0) {
        xmlComponents = new Content[1];
        xmlComponents[0] = new DefaultDocument(xsltConfiguration.getOutputPropertyFactories());
      }
      ContentTask task = new TransformedContent(xmlComponents,
                                                saxSinkFactory,
                                                xsltConfiguration.getOutputPropertyFactories());
      //System.out.println("TransformedContentAssembler cons Leave");
      if (parameterDescriptors.length > 0) {
        task = new ContentTaskPrefilter(task,parameterDescriptors);
      }
      if (catchError != null) {
        task = new ContentTaskCatchError(task, catchError);
      }

      return task;
    } catch (ServingXmlException e) {
      throw e.contextualizeMessage(context.getElement().getTagName());
    } catch (Exception e) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ERROR,
                                                                 context.getElement().getTagName(), e.getMessage());
      throw new ServingXmlException(message,e);
    }
  }
}

