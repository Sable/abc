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

package com.servingxml.components.error;

import com.servingxml.app.ServiceContext;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.components.task.Task;
import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.util.ServingXmlException;
import com.servingxml.app.Flow;

/**
 *
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class CatchErrorImpl implements CatchError {

  private final Task[] tasks;

  public CatchErrorImpl(Task[] tasks) {

    this.tasks = tasks;
  }

  public void catchError(ServiceContext context, Flow flow,
  ServingXmlException fault, StreamSink streamSink) {

    SaxSource errorSource = new ErrorSaxSource(fault, context.getTransformerFactory());
    Flow newFlow = flow.replaceDefaultSaxSource(context, errorSource);

    for (int i = 0; i < tasks.length; ++i) {
      Task task = tasks[i];
      task.execute(context, newFlow);
    }
  }
}

