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

import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.content.Content;
import com.servingxml.components.content.DefaultDocument;
import com.servingxml.components.saxsink.SaxSinkFactory;
import com.servingxml.components.xsltconfig.XsltConfiguration;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.components.content.ContentPrefilter;
import com.servingxml.components.transform.ContentTask;
import com.servingxml.components.transform.ContentTaskCatchError;
import com.servingxml.components.transform.ContentTaskPrefilter;
import com.servingxml.components.error.CatchError;

/**
 * Factory for creating a pipeline component.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 * @see Content
 */               

public class WrappedContentAssembler {

  private Content content = null;
  private SaxSinkFactory saxSinkFactory = SaxSinkFactory.DEFAULT;
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private XsltConfiguration xsltConfiguration = null;
  private CatchError catchError = null;

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {
    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(Content content) {
    this.content = content;
  }

  public void injectComponent(SaxSinkFactory saxSinkFactory) {
    this.saxSinkFactory = saxSinkFactory;
  }

  public void injectComponent(CatchError catchError) {
    this.catchError = catchError;
  }

  public void injectComponent(XsltConfiguration xsltConfiguration) {
    this.xsltConfiguration = xsltConfiguration;
  }

  public ContentTask assemble(ConfigurationContext context) {
    //System.out.println("WrappedContentAssembler.assemble Enter");
    if (xsltConfiguration == null) {
      xsltConfiguration = XsltConfiguration.getDefault();
    }

    if (content == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,context.getElement().getTagName(),
                                                                 "sx:content, sx:transform");
      throw new ServingXmlException(message);
    }

    ContentTask task = new WrappedContent(content,
                                          saxSinkFactory,
                                          xsltConfiguration.getOutputPropertyFactories());
    if (parameterDescriptors.length > 0) {
      task = new ContentTaskPrefilter(task,parameterDescriptors);
    }
    if (catchError != null) {
      task = new ContentTaskCatchError(task, catchError);
    }

    return task;
  }
}

