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

package com.servingxml.components.recordio;

import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.task.Task;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.app.Environment;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public class ProcessRecordFilterAppenderAssembler {

  private Task[] tasks = new Task[0];
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private Name recordTypeName = Name.EMPTY;

  public void setRecordType(Name recordTypeName) {
    this.recordTypeName = recordTypeName;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(Task[] tasks) {
    this.tasks = tasks;
  }

  public RecordFilterAppender assemble(ConfigurationContext context) {

    if (tasks.length == 0) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,
                         context.getElement().getTagName(),"sx:task");
      throw new ServingXmlException(message);
    }

    Environment env = new Environment(parameterDescriptors,context.getQnameContext());
    RecordFilterAppender recordFilterAppender = new ProcessRecordFilterAppender(env, tasks, recordTypeName);

    return recordFilterAppender;

  }
}

