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

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.servingxml.app.ServiceContext;
import com.servingxml.util.record.Record;
import com.servingxml.util.ServingXmlException;
import com.servingxml.app.Flow;
import com.servingxml.components.task.AbstractTask;
import com.servingxml.components.string.Stringable;
import com.servingxml.util.Name;

/**
 * The <code>ThrowError</code> class implements the <code>Task</code> interface.
 *
 *  
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ThrowError extends AbstractTask {

  private final Stringable stringable;

  public ThrowError(Stringable stringable) {

    this.stringable = stringable;
  }

  public void execute(ServiceContext context, Flow flow) {
    String input = stringable.createString(context, flow);
    throw new ServingXmlException(input);
  }

  public String createString(ServiceContext context, Flow flow) {
    String input = stringable.createString(context, flow);
    return input;
  }
}

