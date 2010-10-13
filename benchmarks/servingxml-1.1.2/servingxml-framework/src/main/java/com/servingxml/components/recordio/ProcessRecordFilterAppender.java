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

import com.servingxml.app.ServiceContext;
import com.servingxml.components.task.Task;
import com.servingxml.app.Flow;
import com.servingxml.util.Name;
import com.servingxml.app.Environment;

/**
 *
 * 
 * @author  Daniel A. Parker
 */


class ProcessRecordFilterAppender extends AbstractRecordFilterAppender   
implements RecordFilterAppender {
  private final Environment env;
  private final Task[] tasks;
  private final Name recordTypeName;

  public ProcessRecordFilterAppender(Environment env, Task[] tasks, Name recordTypeName) {
    this.env = env;
    this.tasks = tasks;
    this.recordTypeName = recordTypeName;
  }

  public void appendToRecordPipeline(ServiceContext context, Flow flow,
  RecordFilterChain pipeline) {

    RecordFilter recordFilter = new ProcessRecordFilter(env, tasks, recordTypeName);
    pipeline.addRecordFilter(recordFilter);
  }
}

