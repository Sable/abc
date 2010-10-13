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
import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;

/**
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class RecordWriterFactoryPrefilter extends RecordPipelineAppenderPrefilter   
implements RecordWriterFactory {
  private final RecordWriterFactory recordWriterFactory;
  private final ParameterDescriptor[] parameterDescriptors;

  public RecordWriterFactoryPrefilter(RecordWriterFactory recordWriterFactory,
  ParameterDescriptor[] parameterDescriptors) {
    super(recordWriterFactory, parameterDescriptors);
    this.recordWriterFactory = recordWriterFactory;
    this.parameterDescriptors = parameterDescriptors;
  }

  public RecordWriter createRecordWriter(ServiceContext context, Flow flow) {
    Flow newFlow = flow.augmentParameters(context,parameterDescriptors);
    return recordWriterFactory.createRecordWriter(context, newFlow);
  }
}

