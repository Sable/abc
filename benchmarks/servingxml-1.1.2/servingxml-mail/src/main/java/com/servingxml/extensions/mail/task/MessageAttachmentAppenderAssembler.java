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

package com.servingxml.extensions.mail.task;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.components.task.Task;
import com.servingxml.components.task.TaskPrefilter;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.expr.substitution.SubstitutionExpr;

/**
 * Factory for creating a <tt>MessageAttachmentAppender</tt> instances.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class MessageAttachmentAppenderAssembler {

  private Task[] tasks = new Task[0];
  private String filename = "";
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {
    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(Task[] tasks) {
    this.tasks = tasks;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public PartAppender assemble(final ConfigurationContext context) {

    if (tasks.length == 0) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,
        context.getElement().getTagName(),"sx:task");
      throw new ServingXmlException(message);
    }
    SubstitutionExpr filenameResolver = SubstitutionExpr.parseString(context.getQnameContext(),filename);

    if (parameterDescriptors.length > 0) {
      Task task = new TaskPrefilter(tasks,parameterDescriptors);
      tasks = new Task[]{task};
    }
    PartAppender appender = new MessageAttachmentAppender(tasks,filenameResolver);

    return appender;
  }
}

