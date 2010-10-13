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

package com.servingxml.components.service;

import com.servingxml.app.Service;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.task.TaskPrefilter;
import com.servingxml.components.task.Task;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.ServingXmlException;
import com.servingxml.components.error.CatchError;
import com.servingxml.components.streamsink.StreamSinkFactory;
import com.servingxml.components.streamsource.StreamSourceFactory;
import com.servingxml.components.saxsource.SaxSourceFactory;
import com.servingxml.components.task.TaskCatchError;
import com.servingxml.components.common.NameSubstitutionExpr;

/**
 * Assembler for assembling a <tt>RunService</tt> instance.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class RunServiceAssembler {

  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private Service service;
  private CatchError catchError = null;
  private StreamSinkFactory sinkFactory = null;
  private StreamSourceFactory sourceFactory = null;
  private SaxSourceFactory saxSourceFactory = null;
  private String serviceId = "";

  // Deprecated
  public void setService(String serviceId) {
    this.serviceId = serviceId;
  }

  public void setServiceRef(String serviceId) {
    this.serviceId = serviceId;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {
    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(Service service) {
    this.service = service;
  }

  public void injectComponent(StreamSinkFactory sinkFactory) {
    this.sinkFactory = sinkFactory;
  }

  public void injectComponent(StreamSourceFactory sourceFactory) {
    this.sourceFactory = sourceFactory;
  }

  public void injectComponent(SaxSourceFactory saxSourceFactory) {
    this.saxSourceFactory = saxSourceFactory;
  }

  public void injectComponent(CatchError catchError) {
    this.catchError = catchError;
  }

  public Task assemble(ConfigurationContext context) {
    
    if (service == null && serviceId.length() == 0) {
      String msg = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
                                                             context.getElement().getTagName(),"service");
      throw new ServingXmlException(msg);
    }

    Task task = null;
    if (serviceId.length() > 0) {
      NameSubstitutionExpr serviceIdResolver = NameSubstitutionExpr.parse(context.getQnameContext(),serviceId);
      task = new RunService(serviceIdResolver,sourceFactory,sinkFactory);
    } else {
      task = new RunService2(service,sourceFactory,sinkFactory);
    }
    if (parameterDescriptors.length != 0) {
      task = new TaskPrefilter(task,parameterDescriptors);
    }
    if (catchError != null) {
      task = new TaskCatchError(task,catchError);
    }

    return task;
  }
}

