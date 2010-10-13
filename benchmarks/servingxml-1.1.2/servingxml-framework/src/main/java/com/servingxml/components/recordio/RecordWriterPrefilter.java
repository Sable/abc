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
import com.servingxml.util.ServingXmlException;
import com.servingxml.app.Flow;
import com.servingxml.app.ParameterDescriptor;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public class RecordWriterPrefilter implements RecordWriter {
  private final RecordWriter recordWriter;
  private final ParameterDescriptor[] parameterDescriptors;

  public RecordWriterPrefilter(RecordWriter recordWriter,
  ParameterDescriptor[] parameterDescriptors) {
    this.recordWriter = recordWriter;
    this.parameterDescriptors = parameterDescriptors;
  }

  public void startRecordStream(ServiceContext context, Flow flow) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    recordWriter.startRecordStream(context, newFlow);
  }
  public void endRecordStream(ServiceContext context, Flow flow) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    recordWriter.endRecordStream(context, newFlow);
  }
  public void writeRecord(ServiceContext context, Flow flow) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    recordWriter.writeRecord(context, newFlow);
  }
  public void close() {
    recordWriter.close();
  }
}

