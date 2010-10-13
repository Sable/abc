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

package com.servingxml.components.task;

import com.servingxml.app.ServiceContext;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;
import com.servingxml.app.Flow;
import com.servingxml.components.error.CatchError;
import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.util.Name;

/**
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class TaskCatchError implements Task {
  private final Task task;
  private final CatchError catchError;

  public TaskCatchError(Task task, CatchError catchError) {

    this.task = task;
    this.catchError = catchError;
  }

  public void execute(ServiceContext context, Flow flow) {
    try {
      task.execute(context, flow);
    } catch (ServingXmlException e) {
      StreamSink errOutput = flow.getDefaultStreamSink();
      catchError.catchError(context, flow,e,errOutput);
    }
  }

  public String createString(ServiceContext context, Flow flow) {
    return task.createString(context, flow);
  }
}

