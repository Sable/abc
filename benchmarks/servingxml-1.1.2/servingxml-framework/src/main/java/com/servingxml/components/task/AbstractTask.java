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

import java.io.IOException;
import java.io.ByteArrayOutputStream;

import com.servingxml.app.ServiceContext;
import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.io.streamsink.OutputStreamSinkAdaptor;
import com.servingxml.util.record.Record;
import com.servingxml.app.Flow;
import com.servingxml.util.Name;

/**
 *
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public abstract class AbstractTask implements Task {

  public AbstractTask() {
  }

  public String createString(ServiceContext context, Flow flow) {

    ByteArrayOutputStream os = null;
    try {
      os = new ByteArrayOutputStream();
      StreamSink defaultSink = new OutputStreamSinkAdaptor(os);
      Flow newFlow = flow.replaceDefaultStreamSink(context, defaultSink);

      execute(context, newFlow);
      String s = os.toString();
      return s;
    } finally {
      try {
        if (os != null) {
          os.close();
        }
      } catch (IOException e) {
      } 
    }
  }
}

